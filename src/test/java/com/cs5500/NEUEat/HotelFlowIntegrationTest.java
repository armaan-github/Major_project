package com.cs5500.NEUEat;

import com.cs5500.NEUEat.model.hotelflow.HotelFlowRole;
import com.cs5500.NEUEat.model.hotelflow.HotelFlowStaff;
import com.cs5500.NEUEat.repository.hotelflow.HotelFlowStaffRepository;
import com.cs5500.NEUEat.service.PasswordService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HotelFlowIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private HotelFlowStaffRepository staffRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final PasswordService passwordService = new PasswordService();

  @BeforeEach
  void setup() {
    if (staffRepository.findByUserName("reception-test").isEmpty()) {
      staffRepository.save(new HotelFlowStaff("reception-test",
          passwordService.generatePassword("reception123"), HotelFlowRole.RECEPTION));
    }
  }

  @Test
  void fullFlowBookingToCheckout() throws Exception {
    String receptionToken = loginStaffAndGetToken("reception-test", "reception123");

    String guestBody = "{" +
        "\"userName\":\"guest-int\"," +
        "\"password\":\"guest123\"," +
        "\"phoneNumber\":\"1111111111\"," +
        "\"address\":\"1 Test St\"," +
        "\"city\":\"Boston\"," +
        "\"state\":\"MA\"," +
        "\"zip\":\"02111\"" +
        "}";

    String guestResponse = mockMvc.perform(post("/api/hotelflow/guest/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(guestBody))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    String guestId = objectMapper.readTree(guestResponse).get("id").asText();

    String roomBody = "{" +
        "\"roomNumber\":\"777\"," +
        "\"roomType\":\"Suite\"," +
        "\"capacity\":2," +
        "\"nightlyRate\":200" +
        "}";

    String roomResponse = mockMvc.perform(post("/api/hotelflow/room/create")
            .header("Authorization", "Bearer " + receptionToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(roomBody))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    String roomId = objectMapper.readTree(roomResponse).get("id").asText();

    String bookingBody = "{" +
        "\"guestId\":\"" + guestId + "\"," +
        "\"roomId\":\"" + roomId + "\"," +
        "\"checkInDate\":\"2026-04-14\"," +
        "\"checkOutDate\":\"2026-04-16\"" +
        "}";

    String bookingResponse = mockMvc.perform(post("/api/hotelflow/booking/create")
            .header("Authorization", "Bearer " + receptionToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(bookingBody))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    String bookingId = objectMapper.readTree(bookingResponse).get("id").asText();

    mockMvc.perform(post("/api/hotelflow/booking/checkin")
            .header("Authorization", "Bearer " + receptionToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"bookingId\":\"" + bookingId + "\"}"))
        .andExpect(status().isOk());

    String guestToken = loginGuestAndGetToken("guest-int", "guest123");

    String orderResponse = mockMvc.perform(post("/api/hotelflow/order/qr")
            .header("Authorization", "Bearer " + guestToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                "\"bookingId\":\"" + bookingId + "\"," +
                "\"items\":[{" +
                "\"itemName\":\"Coffee\"," +
                "\"quantity\":2," +
                "\"unitPrice\":5.0" +
                "}]" +
                "}"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    JsonNode orderNode = objectMapper.readTree(orderResponse);
    assertThat(orderNode.get("postedToFolio").asBoolean()).isTrue();

    String folioResponse = mockMvc.perform(get("/api/hotelflow/folio/" + bookingId)
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    JsonNode folioNode = objectMapper.readTree(folioResponse);
    assertThat(folioNode.get("lines").size()).isGreaterThanOrEqualTo(2);

    String checkoutResponse = mockMvc.perform(post("/api/hotelflow/checkout")
            .header("Authorization", "Bearer " + receptionToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"bookingId\":\"" + bookingId + "\"}"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    JsonNode invoiceNode = objectMapper.readTree(checkoutResponse);
    assertThat(invoiceNode.get("id").asText()).isNotBlank();
    assertThat(invoiceNode.get("total").asDouble()).isGreaterThan(0.0);
  }

  private String loginStaffAndGetToken(String userName, String password) throws Exception {
    String response = mockMvc.perform(post("/api/hotelflow/auth/staff-login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"userName\":\"" + userName + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(response).get("token").asText();
  }

  private String loginGuestAndGetToken(String userName, String password) throws Exception {
    String response = mockMvc.perform(post("/api/hotelflow/auth/guest-login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"userName\":\"" + userName + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(response).get("token").asText();
  }
}
