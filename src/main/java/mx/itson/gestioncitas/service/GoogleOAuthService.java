/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.itson.gestioncitas.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
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
 * 1. Abre el navegador para que el usuario autorice.
 * 2. Intercambia el código por tokens.
 * 3. Persiste usuario y tokens en la BD.
 *
 * Uso típico:
 * <pre>
 *   GoogleOAuthService oauth = new GoogleOAuthService(usuarioRepo, tokenRepo);
 *   UsuarioGoogle usuario = oauth.iniciarSesion();
 * </pre>
 */
public class GoogleOAuthService {

    private static final String APP_NAME    = "GestionCitasMedicas";
    private static final GsonFactory JSON   = GsonFactory.getDefaultInstance();

    // Scopes: leer/escribir calendario + info del perfil
    private static final List<String> SCOPES = Arrays.asList(
            CalendarScopes.CALENDAR,
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
    );

    private final IUsuarioGoogleRepository usuarioRepo;
    private final IGoogleTokenRepository   tokenRepo;

    // Estado post-login
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
     * obtiene la info del usuario y persiste todo en la BD.
     *
     * @return UsuarioGoogle autenticado y persistido
     * @throws IOException              si falla la red o no se encuentra credentials.json
     * @throws GeneralSecurityException si falla el TLS
     */
    public UsuarioGoogle iniciarSesion() throws IOException, GeneralSecurityException {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Carga el archivo credentials.json descargado de Google Cloud Console
        InputStream credStream = getClass().getResourceAsStream("/credentials.json");
        if (credStream == null) {
            throw new FileNotFoundException(
                    "No se encontró credentials.json en src/main/resources/. " +
                    "Descárgalo desde Google Cloud Console → Credenciales.");
        }

        GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON, new InputStreamReader(credStream));

        // Construye el flujo SIN FileDataStoreFactory: guardamos el token en nuestra BD
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON, secrets, SCOPES)
                .setAccessType("offline")   // solicita refresh_token
                .build();

        // Abre el navegador y escucha en localhost:8888
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888).build();

        credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        // Obtiene info del perfil
        Userinfo perfil = obtenerPerfil();

        // Persiste / actualiza usuario en BD
        UsuarioGoogle usuario = new UsuarioGoogle(
                perfil.getId(),
                perfil.getName(),
                perfil.getEmail(),
                perfil.getPicture()
        );
        usuarioActual = usuarioRepo.guardarOActualizar(usuario);

        // Persiste token en BD
        LocalDateTime expira = credential.getExpiresInSeconds() != null
                ? LocalDateTime.now().plusSeconds(credential.getExpiresInSeconds())
                : LocalDateTime.now().plusHours(1);

        GoogleToken token = new GoogleToken(
                usuarioActual.getId(),
                credential.getAccessToken(),
                credential.getRefreshToken(),
                "Bearer",
                expira,
                String.join(" ", SCOPES)
        );
        tokenRepo.guardarOActualizar(token);

        System.out.println("[GoogleOAuthService] Login exitoso: " + usuarioActual.getCorreo());
        return usuarioActual;
    }

    /**
     * Cierra la sesión del usuario: elimina su token de la BD.
     */
    public void cerrarSesion() {
        if (usuarioActual != null) {
            tokenRepo.eliminarPorUsuario(usuarioActual.getId());
            usuarioActual = null;
            credential    = null;
            System.out.println("[GoogleOAuthService] Sesión cerrada.");
        }
    }

    // ── getters para los demás services ──────────────────────────────────────

    public Credential       getCredential()    { return credential; }
    public NetHttpTransport getHttpTransport()  { return httpTransport; }
    public GsonFactory      getJsonFactory()    { return JSON; }
    public String           getAppName()        { return APP_NAME; }
    public UsuarioGoogle    getUsuarioActual()  { return usuarioActual; }

    // ── privados ─────────────────────────────────────────────────────────────

    private Userinfo obtenerPerfil() throws IOException {
        Oauth2 oauth2 = new Oauth2.Builder(httpTransport, JSON, credential)
                .setApplicationName(APP_NAME)
                .build();
        return oauth2.userinfo().get().execute();
    }
}
