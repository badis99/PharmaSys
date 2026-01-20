package pharmacie.util;

import pharmacie.exception.ValidationException;

public class ValidationUtils {

    /**
     * Validates a phone number based on specific rules:
     * - Must be exactly 8 digits.
     * - Must start with '5', '9', or '2'.
     *
     * @param phone The phone number to validate.
     * @throws ValidationException if the phone number is invalid.
     */
    public static void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return; // Allow null/empty if the field is optional in the DB
        }

        String trimmed = phone.trim();

        if (!trimmed.matches("\\d{8}")) {
            throw new ValidationException("Le numéro de téléphone doit être composé d'exactement 8 chiffres.");
        }

        char firstChar = trimmed.charAt(0);
        if (firstChar != '2' && firstChar != '5' && firstChar != '9') {
            throw new ValidationException("Le numéro de téléphone doit commencer par '2', '5' ou '9'.");
        }
    }
}
