package dev.westernpine.site.routes;

import com.google.gson.JsonSyntaxException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.object.Client;
import dev.westernpine.site.Site;
import spark.Request;
import spark.Response;
import spark.Route;

public class StripeWebhook implements Route {

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String payload = request.body();
        String sigHeader = request.headers("Stripe-Signature");

        Event event = null;

        try {
            event = Webhook.constructEvent(payload, sigHeader, Site.stripeWebhookSignature);
        } catch (JsonSyntaxException e) {
            response.status(400);
            System.out.println("Invalid webhook payload!");
            return null;
        } catch (SignatureVerificationException e) {
            response.status(400);
            System.out.println("Invalid webhook signature!");
            return null;
        }

        StripeObject stripeObject = null;
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer.getObject().isPresent())
            stripeObject = dataObjectDeserializer.getObject().get();
        else
            throw new RuntimeException("Failed to deserialize any object from the json data!");

        System.out.println(event.getType());

        if(event.getType().startsWith("customer.subscription")) {
            Subscription subscription = (Subscription) stripeObject;
            Site.clientManager.getByCustomer(subscription.getCustomer())
                    .onSuccess(client -> Site.manager.updatePremium(client.getUserId(), client.setPremiumStatus(Client.Status.fromStripeStatus(subscription.getStatus())).isActive()))
//                    .onFailure(throwable -> System.out.println("An error occurred with subscription: " + subscription.toJson()))
            ;
        }

        response.status(200);
        return "";
    }

}