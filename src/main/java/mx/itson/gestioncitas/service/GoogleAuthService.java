/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.itson.gestioncitas.service;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import mx.itson.gestioncitas.model.GoogleToken;
import mx.itson.gestioncitas.model.UsuarioGoogle;
import mx.itson.gestioncitas.repository.IGoogleTokenRepository;
import mx.itson.gestioncitas.repository.IUsuarioGoogleRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Gestiona el estado de autenticación:
 * - Si el usuario ya tiene token válido en BD, lo reutiliza sin abrir el navegador.
 * - Si no, delega al flujo OAuth2 completo (GoogleOAuthService).
 *
 * Uso típico en Main o en el flujo de agendar:
 * <pre>
 *   GoogleAuthService auth = new GoogleAuthService(oauthSvc, usuarioRepo, tokenRepo);
 *   UsuarioGoogle usuario = auth.autenticar("correo@gmail.com"); // solo abre navegador si es necesario
 * </pre>
 */
public class GoogleAuthService {

    private static final GsonFactory JSON = GsonFactory.getDefaultInstance();
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";

    private final GoogleOAuthService       oauthService;
    private final IUsuarioGoogleRepository usuarioRepo;
    private final IGoogleTokenRepository   tokenRepo;

    private Credential       credentialActual;
    private NetHttpTransport httpTransport;

    public GoogleAuthService(GoogleOAuthService oauthService,
                             IUsuarioGoogleRepository usuarioRepo,
                             IGoogleTokenRepository tokenRepo) {
        this.oauthService = oauthService;
        this.usuarioRepo  = usuarioRepo;
        this.tokenRepo    = tokenRepo;
    }

    /**
     * Autentica al usuario. Si ya existe un token válido en BD para ese correo,
     * lo reutiliza. Si no, abre el navegador para el flujo OAuth2.
     *
     * @param correo correo del usuario (puede ser null para iniciar sesión nueva)
     * @return usuario autenticado
     */
    public UsuarioGoogle autenticar(String correo) throws IOException, GeneralSecurityException {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        if (correo != null) {
            Optional<UsuarioGoogle> usuarioOpt = usuarioRepo.buscarPorCorreo(correo);
            if (usuarioOpt.isPresent()) {
                UsuarioGoogle usuario = usuarioOpt.get();
                Optional<GoogleToken> tokenOpt = tokenRepo.buscarPorUsuario(usuario.getId());

                if (tokenOpt.isPresent() && tokenOpt.get().getRefreshToken() != null) {
                    // Token existe en BD: construye Credential con el refresh_token
                    credentialActual = construirCredentialDesdeDB(tokenOpt.get());
                    System.out.println("[GoogleAuthService] Token recuperado desde BD para: " + correo);
                    return usuario;
                }
            }
        }

        // Sin token en BD: hace el flujo completo
        UsuarioGoogle usuario = oauthService.iniciarSesion();
        credentialActual = oauthService.getCredential();
        httpTransport    = oauthService.getHttpTransport();
        return usuario;
    }

    /**
     * Reconstruye un objeto Credential a partir del refresh_token guardado en BD,
     * permitiendo llamadas a la API sin volver a abrir el navegador.
     */
    private Credential construirCredentialDesdeDB(GoogleToken token) throws IOException {
        InputStream credStream = getClass().getResourceAsStream("/credentials.json");
        if (credStream == null) {
            throw new RuntimeException("credentials.json no encontrado en resources.");
        }

        GoogleClientSecrets secrets = GoogleClientSecrets.load(
                JSON, new InputStreamReader(credStream));

        String clientId     = secrets.getDetails().getClientId();
        String clientSecret = secrets.getDetails().getClientSecret();

        Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(httpTransport)
                .setJsonFactory(JSON)
                .setTokenServerUrl(new GenericUrl(TOKEN_URL))
                .setClientAuthentication(
                        new ClientParametersAuthentication(clientId, clientSecret))
                .build();

        credential.setAccessToken(token.getAccessToken());
        credential.setRefreshToken(token.getRefreshToken());

        // Calcula cuántos segundos faltan para expirar
        if (token.getExpiresAt() != null) {
            long segundosRestantes = java.time.Duration
                    .between(LocalDateTime.now(), token.getExpiresAt())
                    .getSeconds();
            credential.setExpiresInSeconds(Math.max(segundosRestantes, 0));
        }

        return credential;
    }

    // ── getters para CalendarSyncService / GoogleCalendarService ─────────────

    public Credential       getCredential()   { return credentialActual; }
    public NetHttpTransport getHttpTransport() { return httpTransport; }
    public GsonFactory      getJsonFactory()   { return JSON; }
    public String           getAppName()       { return "GestionCitasMedicas"; }
}
