package com.ejemplo.tienda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OrderFlowTests {

    @Autowired
    MockMvc mvc;

    @Test
    void anOrderTakesItsUnitsOutOfTheCatalogStock() throws Exception {
        long productId = createProduct("Teclado", "120000", 30);

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order("C-001", productId, 3)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.total").value(360000))
                .andExpect(jsonPath("$.lines[0].productName").value("Teclado"));

        mvc.perform(get("/api/products/" + productId))
                .andExpect(jsonPath("$.stock").value(27));
    }

    @Test
    void anOrderExceedingStockIsSavedAsRejectedWithZeroTotal() throws Exception {
        long scarce = createProduct("Teclado limitado", "120000", 2);

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order("C-001", scarce, 5)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("RECHAZADO"))
                .andExpect(jsonPath("$.total").value(0));

        mvc.perform(get("/api/products/" + scarce))
                .andExpect(jsonPath("$.stock").value(2));
    }

    @Test
    void anOrderForAnUnknownProductReturns404() throws Exception {
        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order("C-001", 999_999L, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Producto no encontrado"));
    }

    @Test
    void anInvalidOrderReturns400() throws Exception {
        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anExistingOrderCanBeReadBackWithItsLines() throws Exception {
        long productId = createProduct("Teclado Pro", "120000", 10);
        String created = mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order("C-002", productId, 4)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long orderId = ((Number) JsonPath.read(created, "$.id")).longValue();

        mvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.total").value(441600));
    }

    @Test
    void aProductWithOrdersCannotBeDeleted() throws Exception {
        long productId = createProduct("Teclado Test", "120000", 5);
        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order("C-001", productId, 1)))
                .andExpect(status().isCreated());

        mvc.perform(delete("/api/products/" + productId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Producto en uso"));
    }

    // --- Pruebas Hoja 2: Q0 Idempotencia por codigoExterno ---

    @Test
    void q0_postRepetidoConMismoCodigoExternoYMismoPayloadEsIdempotente() throws Exception {
        long productId = createProduct("Teclado Idempotente", "120000", 20);
        String payload = orderWithExternalCode("ORD-EXT-900", "C-001", productId, 4);

        String primeraRespuesta = mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoExterno").value("ORD-EXT-900"))
                .andExpect(jsonPath("$.total").value(441600))
                .andReturn().getResponse().getContentAsString();

        String segundaRespuesta = mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoExterno").value("ORD-EXT-900"))
                .andReturn().getResponse().getContentAsString();

        long id1 = ((Number) JsonPath.read(primeraRespuesta, "$.id")).longValue();
        long id2 = ((Number) JsonPath.read(segundaRespuesta, "$.id")).longValue();
        assertThat(id2).isEqualTo(id1);

        // El stock solo se descontó una vez (20 - 4 = 16)
        mvc.perform(get("/api/products/" + productId))
                .andExpect(jsonPath("$.stock").value(16));
    }

    @Test
    void q0_postConMismoCodigoExternoYPayloadDiferenteRetorna409Conflict() throws Exception {
        long productId = createProduct("Teclado Conflicto", "120000", 20);
        String payloadOriginal = orderWithExternalCode("ORD-EXT-901", "C-001", productId, 2);
        String payloadModificado = orderWithExternalCode("ORD-EXT-901", "C-001", productId, 5);

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadOriginal))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadModificado))
                .andExpect(status().isConflict());

        // El stock solo refleja el pedido original (20 - 2 = 18)
        mvc.perform(get("/api/products/" + productId))
                .andExpect(jsonPath("$.stock").value(18));
    }

    private static String order(String clienteId, long productId, int quantity) {
        return "{\"clienteId\":\"" + clienteId + "\",\"items\":[{\"productId\":" + productId + ",\"quantity\":" + quantity + "}]}";
    }

    private static String orderWithExternalCode(String codigoExterno, String clienteId, long productId, int quantity) {
        return "{\"codigoExterno\":\"" + codigoExterno + "\",\"clienteId\":\"" + clienteId
                + "\",\"items\":[{\"productId\":" + productId + ",\"quantity\":" + quantity + "}]}";
    }

    private long createProduct(String name, String price, int stock) throws Exception {
        String body = mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"description\":\"d\","
                                + "\"price\":" + price + ",\"stock\":" + stock
                                + ",\"category\":\"electronica\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }
}