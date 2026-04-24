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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Gestiona el estado de autenticación del admin:
 *  - Si ya tiene token válido en BD, lo reutiliza sin abrir el navegador.
 *  - Si no, delega al flujo OAuth2 completo (GoogleOAuthService).
 */
public class GoogleAuthService {

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
     * Autentica al admin. Reutiliza el token en BD si existe y es válido;
     * de lo contrario abre el navegador para el flujo OAuth2 completo.
     *
     * @param correo correo del admin (null para forzar login nuevo)
     * @return usuario autenticado
     * @throws IOException              si falla la red
     * @throws GeneralSecurityException si falla el TLS
     */
    public UsuarioGoogle autenticar(String correo) throws IOException, GeneralSecurityException {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        if (correo != null) {
            Optional<UsuarioGoogle> usuarioOpt = usuarioRepo.buscarPorCorreo(correo);
            if (usuarioOpt.isPresent()) {
                UsuarioGoogle usuario = usuarioOpt.get();
                Optional<GoogleToken> tokenOpt = tokenRepo.buscarPorUsuario(usuario.getId());
                if (tokenOpt.isPresent() && tokenOpt.get().getRefreshToken() != null) {
                    credentialActual = construirCredentialDesdeDB(tokenOpt.get());
                    System.out.println("[GoogleAuthService] Sesión recuperada para: " + correo);
                    return usuario;
                }
            }
        }

        // Sin token válido en BD → flujo OAuth2 completo
        UsuarioGoogle usuario = oauthService.iniciarSesion();
        credentialActual = oauthService.getCredential();
        httpTransport    = oauthService.getHttpTransport();
        return usuario;
    }

    public Credential       getCredential()   { return credentialActual; }
    public NetHttpTransport getHttpTransport() { return httpTransport; }
    public GsonFactory      getJsonFactory()   { return GoogleOAuthService.JSON; }
    public String           getAppName()       { return GoogleOAuthService.APP_NAME; }

    private Credential construirCredentialDesdeDB(GoogleToken token) throws IOException {
        InputStream credStream = getClass().getResourceAsStream("/credentials.json");
        if (credStream == null)
            throw new RuntimeException("credentials.json no encontrado en resources.");

        GoogleClientSecrets secrets = GoogleClientSecrets.load(
                GoogleOAuthService.JSON, new InputStreamReader(credStream));

        Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(httpTransport)
                .setJsonFactory(GoogleOAuthService.JSON)
                .setTokenServerUrl(new GenericUrl(TOKEN_URL))
                .setClientAuthentication(new ClientParametersAuthentication(
                        secrets.getDetails().getClientId(),
                        secrets.getDetails().getClientSecret()))
                .build();

        credential.setAccessToken(token.getAccessToken());
        credential.setRefreshToken(token.getRefreshToken());

        if (token.getExpiresAt() != null) {
            long segs = Duration.between(LocalDateTime.now(), token.getExpiresAt()).getSeconds();
            credential.setExpiresInSeconds(Math.max(segs, 0));
        }
        return credential;
    }
}