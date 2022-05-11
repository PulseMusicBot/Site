package dev.westernpine.lib.object;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import dev.westernpine.bettertry.Try;
import dev.westernpine.site.Site;

import java.util.*;
import java.util.stream.Stream;

public class Client {

    public static enum Status {

        ACTIVE(Arrays.asList("trialing", "active")),
        PENDING(Arrays.asList("incomplete", "past_due")),
        INACTIVE(Arrays.asList("incomplete_expired", "canceled", "unpaid"));

        public static Status fromStripeStatus(String stripeStatus) {
            return Stream.of(Status.values()).filter(status -> status.stripeStatuses.contains(stripeStatus)).findAny().orElse(Status.INACTIVE);
        }

        private List<String> stripeStatuses;

        Status(List<String> stripeStatuses) {
            this.stripeStatuses = stripeStatuses;
        }

        public List<String> getStripeStatuses() {
            return stripeStatuses;
        }

        public boolean isActive() {
            return this.equals(ACTIVE);
        }

        public boolean isPending() {
            return this.equals(PENDING);
        }

        public boolean isInactive() {
            return this.equals(INACTIVE);
        }
    }

    private String userId;
    private String customerId;
    private Status premiumStatus;

    public Client(String userId, String customerId) {
        this.userId = userId;
        this.customerId = customerId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Status getPremiumStatus() {
        if(premiumStatus != null)
            return premiumStatus;
        return Try.to(this::updatePremiumStatus).orElseTry(() -> premiumStatus = Status.INACTIVE).getUnchecked();
    }

    public Status updatePremiumStatus() throws StripeException {
        List<String> expandList = new ArrayList<>();
        expandList.add("subscriptions");
        Map<String, Object> params = new HashMap<>();
        params.put("expand", expandList);
        Customer customer = Customer.retrieve(customerId, params, null);
        SubscriptionCollection subs = customer.getSubscriptions();
        List<Subscription> subscriptions = subs != null ? subs.getData() : new ArrayList<>();
        for(Subscription subscription : subscriptions)
            return premiumStatus = Status.fromStripeStatus(subscription.getStatus());
        //TODO: Ensure checks for proper product in for loop!
        return premiumStatus = Status.INACTIVE;
    }

    public Status setPremiumStatus(Status status) {
        return this.premiumStatus = status;
    }

    public boolean isUser(String userId) {
        return this.userId.equals(userId);
    }

    public boolean isCustomer(String customerId) {
        return this.customerId.equals(customerId);
    }

    public boolean isClient(Client client) {
        return this.isUser(client.getUserId()) || this.isCustomer(client.customerId);
    }

}