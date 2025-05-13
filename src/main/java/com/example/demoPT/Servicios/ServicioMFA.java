package com.example.demoPT.Servicios;

import com.example.demoPT.Controladores.mfaControlador;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;

@Service
public class ServicioMFA {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public String generateNewSecret() {
        return new mfaControlador().generateHumanFriendlySecret();
    }

    public String generateQRUrl(String username, String secret) {
        String normalizedSecret = secret.replace(" ", "");
        String qrContent = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                "PawsTrack",
                username,
                normalizedSecret,
                "PawsTrack");

        return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data="
                + URLEncoder.encode(qrContent, StandardCharsets.UTF_8); 
    }

    public boolean verifyCode(String secret, String codeInput) {
        try {
            // Verifica que sea exactamente 6 dígitos
            if (codeInput == null || !codeInput.matches("\\d{6}")) {
                return false;
            }
            int code = Integer.parseInt(codeInput);
            return gAuth.authorize(secret, code);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isMfaRequired() {
        return true;

    }
}
