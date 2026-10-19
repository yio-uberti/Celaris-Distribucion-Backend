package com.api.servicio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.SubscriptionPurchaseV2;
import com.google.api.services.androidpublisher.model.SubscriptionPurchasesAcknowledgeRequest;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import jakarta.annotation.PostConstruct;

@Service
public class GooglePlayService {

	@Value("${GOOGLE_PLAY_SA_JSON}") private String saJson;
    @Value("${PLAY_PACKAGE_NAME}") private String packageName;
    private AndroidPublisher publisher;

    @PostConstruct
    void init() throws Exception {
        GoogleCredentials cred = GoogleCredentials
            .fromStream(new ByteArrayInputStream(saJson.getBytes(StandardCharsets.UTF_8)))
            .createScoped(AndroidPublisherScopes.ANDROIDPUBLISHER);
        publisher = new AndroidPublisher.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(cred))
            .setApplicationName("Celaris").build();
    }

    public SubscriptionPurchaseV2 consultar(String purchaseToken) throws IOException {
        return publisher.purchases().subscriptionsv2().get(packageName, purchaseToken).execute();
    }

    public void confirmar(String productId, String purchaseToken) throws IOException {
        publisher.purchases().subscriptions()
            .acknowledge(packageName, productId, purchaseToken, new SubscriptionPurchasesAcknowledgeRequest())
            .execute();
    }
}
