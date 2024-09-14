package com.example.rayzi.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;

public class GooglePlayPurchase {

    private static final String TAG = "GooglePlayPurchase";
    public BillingClient billingClient;
    Activity context;
    OnPurchasedDone onPurchasedDone;
    private String productKey;
    private String type;
    PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
        Log.d(TAG, "onPurchasesUpdated: 1");
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            Log.d(TAG, "onPurchasesUpdated: size  " + purchases.size());

            for (Purchase purchase : purchases) {
                verifySubPurchase(purchase, " -new");
            }
        } else {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }
    };

    public GooglePlayPurchase() {
    }

    public static GooglePlayPurchase getInstance() {
        return Holder.INSTANCE;
    }

    void verifySubPurchase(Purchase purchase, String aNew) {
        onPurchasedDone.onPurchasedDone(purchase, type);
        if (type.equals(BillingClient.ProductType.INAPP)){
            ConsumePurchase(purchase);
        }else {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                    Log.d(TAG, "verifySubPurchase: " + purchase.getOrderId());

                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                        if (type.equals(BillingClient.ProductType.INAPP)) {
                          //  ConsumePurchase(purchase);
                        }
                    } else {
                        Log.e(TAG, "Acknowledge failed: " + billingResult.getDebugMessage());
                    }
                });
            } else {
                Log.d(TAG, "Purchase already acknowledged: " + purchase.getPurchaseToken());

            }
        }
    }

    void ConsumePurchase(Purchase purchase) {



        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        billingClient.consumeAsync(consumeParams, (billingResult, purchaseToken) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Consuming Successful: " + purchaseToken);
            } else {
                Log.e(TAG, "Consume failed: " + billingResult.getDebugMessage());
            }
        });
    }

    public void initGooglePlayPurchase(Activity context, OnPurchasedDone onPurchasedDone) {
        this.context = context;
        this.onPurchasedDone = onPurchasedDone;
        billingClient = BillingClient.newBuilder(context.getApplicationContext())
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "onBillingSetupFinished: ");
                    onPurchasedDone.initied();
                    checkPurchases();
                } else {
                    Log.e(TAG, "Billing setup failed: " + billingResult.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "onBillingServiceDisconnected: ");
            }
        });
    }

    private void checkPurchases() {
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                (billingResult, purchases) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase purchase : purchases) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                                verifySubPurchase(purchase, " -old");
                            }
                        }
                    } else {
                        Log.e(TAG, "Query purchases failed: " + billingResult.getDebugMessage());
                    }
                }
        );
    }

    public void setUpSku(String productKey, String type) {
        this.productKey = productKey;
        this.type = type;
        if (billingClient.isReady()) {
            QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                    .setProductList(
                            ImmutableList.of(
                                    QueryProductDetailsParams.Product.newBuilder()
                                            .setProductId(productKey)
                                            .setProductType(type)
                                            .build()))
                    .build();

            billingClient.queryProductDetailsAsync(
                    queryProductDetailsParams,
                    (billingResult, productDetailsList) -> {
                        Log.d(TAG, "onProductDetailsResponse: " + productDetailsList.size());
                        if (productDetailsList.isEmpty()) {
                            Toast.makeText(context, "Failed 2", Toast.LENGTH_SHORT).show();
                        } else {
                            launchPayment(productDetailsList.get(0), type);
                        }
                    }
            );
        } else {
            Log.e(TAG, "Billing client not ready");
        }
    }

    private void launchPayment(ProductDetails productDetails, String type) {
        Log.d(TAG, "launchPayment: " + productDetails.toString());

        ArrayList<BillingFlowParams.ProductDetailsParams> productList = new ArrayList<>();

        if (type.equals(BillingClient.ProductType.INAPP)) {
            productList.add(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
            );
        } else {
            productList.add(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(productDetails.getSubscriptionOfferDetails().get(0).getOfferToken())
                            .build()
            );
        }

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productList)
                .build();

        BillingResult billingResult = billingClient.launchBillingFlow(context, billingFlowParams);
        if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Launch billing flow failed: " + billingResult.getDebugMessage());
        }
    }

    public void consumeOldPurchase() {
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(), (billingResult, list) -> {


            Log.d(TAG, "run: subs " + list.size());
            String text = "null";
            for (int i = 0; i < list.size(); i++) {
                text = list.get(i).getOrderId().toString();

            }
            for (Purchase p : list
            ) {
                ConsumePurchase(p);

            }


        });

    }

    public interface OnPurchasedDone {
        void onPurchasedDone(Purchase purchase, String type);

        void initied();
    }

    private static final class Holder {
        @SuppressLint("StaticFieldLeak")
        private static final GooglePlayPurchase INSTANCE = new GooglePlayPurchase();
    }
}
