package mx.itson.gestioncitas.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import mx.itson.gestioncitas.model.GoogleToken;
import mx.itson.gestioncitas.model.UsuarioGoogle;
import mx.itson.gestioncitas.repository.IGoogleTokenRepository;
import mx.itson.gestioncitas.repository.IUsuarioGoogleRepository;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Maneja el flujo OAuth2 con Google:
 *  1. Abre el navegador para que el admin autorice el acceso.
 *  2. Intercambia el código por tokens de acceso y refresco.
 *  3. Obtiene el perfil del usuario y persiste usuario + tokens en BD.
 */
public class GoogleOAuthService {

    static final String       APP_NAME = "GestionCitasMedicas";
    static final GsonFactory  JSON     = GsonFactory.getDefaultInstance();

    static final List<String> SCOPES = Arrays.asList(
            CalendarScopes.CALENDAR,
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
    );

    private final IUsuarioGoogleRepository usuarioRepo;
    private final IGoogleTokenRepository   tokenRepo;

    private Credential       credential;
    private NetHttpTransport httpTransport;
    private UsuarioGoogle    usuarioActual;

    public GoogleOAuthService(IUsuarioGoogleRepository usuarioRepo,
                              IGoogleTokenRepository tokenRepo) {
        this.usuarioRepo = usuarioRepo;
        this.tokenRepo   = tokenRepo;
    }

    /**
     * Inicia el flujo OAuth2: abre el navegador, espera el callback en localhost:8888,
     * obtiene el perfil del usuario y persiste todo en BD.
     *
     * @return UsuarioGoogle autenticado y persistido
     * @throws IOException              si falla la red o no se encuentra credentials.json
     * @throws GeneralSecurityException si falla el TLS
     */
    public UsuarioGoogle iniciarSesion() throws IOException, GeneralSecurityException {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        InputStream credStream = getClass().getResourceAsStream("/credentials.json");
        if (credStream == null)
            throw new FileNotFoundException(
                    "credentials.json no encontrado en src/main/resources/. "
                            + "Descárgalo desde Google Cloud Console → Credenciales → OAuth 2.0.");

        GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON, new InputStreamReader(credStream));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON, secrets, SCOPES)
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        Userinfo perfil = obtenerPerfil();

        UsuarioGoogle usuario = new UsuarioGoogle(
                perfil.getId(), perfil.getName(), perfil.getEmail(), perfil.getPicture());
        usuarioActual = usuarioRepo.guardarOActualizar(usuario);

        LocalDateTime expira = credential.getExpiresInSeconds() != null
                ? LocalDateTime.now().plusSeconds(credential.getExpiresInSeconds())
                : LocalDateTime.now().plusHours(1);

        tokenRepo.guardarOActualizar(new GoogleToken(
                usuarioActual.getId(),
                credential.getAccessToken(),
                credential.getRefreshToken(),
                "Bearer", expira,
                String.join(" ", SCOPES)
        ));

        System.out.println("[GoogleOAuthService] Login exitoso: " + usuarioActual.getCorreo());
        return usuarioActual;
    }

    /** Elimina el token del usuario en BD (logout). */
    public void cerrarSesion() {
        if (usuarioActual != null) {
            tokenRepo.eliminarPorUsuario(usuarioActual.getId());
            usuarioActual = null;
            credential    = null;
        }
    }

    public Credential       getCredential()   { return credential; }
    public NetHttpTransport getHttpTransport() { return httpTransport; }
    public GsonFactory      getJsonFactory()   { return JSON; }
    public String           getAppName()       { return APP_NAME; }
    public UsuarioGoogle    getUsuarioActual() { return usuarioActual; }

    private Userinfo obtenerPerfil() throws IOException {
        return new Oauth2.Builder(httpTransport, JSON, credential)
                .setApplicationName(APP_NAME).build()
                .userinfo().get().execute();
    }
}