package ezy.payment.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreatePaymentInputDto {

    @NotBlank(message = "First name is required")
    @Pattern(regexp = "^[a-zA-Z\\s'-]{1,50}$", message = "First name must contain only letters, spaces, hyphens, and apostrophes (max 50 chars)")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Pattern(regexp = "^[a-zA-Z\\s'-]{1,50}$", message = "Last name must contain only letters, spaces, hyphens, and apostrophes (max 50 chars)")
    private String lastName;

    @NotBlank(message = "Expiry is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Expiry must be in MM/YY format")
    private String expiry;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^\\d{3,4}$", message = "CVV must be 3-4 digits")
    private String cvv;

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{13,19}", message = "Card number must be 13-19 digits")
    private String cardNumber;

    public CreatePaymentInputDto() {}

    public CreatePaymentInputDto(String firstName, String lastName, String expiry, String cvv, String cardNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.expiry = expiry;
        this.cvv = cvv;
        this.cardNumber = cardNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}
