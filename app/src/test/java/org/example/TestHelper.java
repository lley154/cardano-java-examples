package org.example;

import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.bloxbean.cardano.client.api.UtxoSupplier;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.DefaultUtxoSupplier;
import com.bloxbean.cardano.client.backend.model.TransactionContent;
import com.bloxbean.cardano.client.util.JsonUtil;

import lombok.NonNull;

import static com.bloxbean.cardano.client.common.CardanoConstants.LOVELACE;

public class TestHelper {

    private final BackendService backendService;

    public TestHelper(BackendService backendService) {
        this.backendService = backendService;
    }

    public List<Utxo> utxos(String address) {
        
         UtxoSupplier utxoSupplier = new DefaultUtxoSupplier(backendService.getUtxoService());
        
        List<Utxo> utxoList = utxoSupplier.getAll(address);
        if (utxoList == null)
            return Collections.emptyList();
        else
            return utxoList;
    }

    public List<Amount> amounts(String address) {
        return amounts(utxos(address));
    }

    public BigInteger lovelaceBalance(String address) {
        return assetBalance(address, LOVELACE);
    }

    public BigInteger assetBalance(String address, String unit) {
        try {
            List<Amount> amounts = amounts(address);
            return assetBalance(unit, amounts).orElse(BigInteger.ZERO);
        } catch (Exception e) {
            return BigInteger.ZERO;
        }
    }

    public static List<Amount> amounts(@NonNull List<Utxo> utxoList) {
        Map<String, List<Amount>> amountMap = utxoList.stream()
                .flatMap(utxo -> utxo.getAmount().stream())
                .collect(Collectors.groupingBy(Amount::getUnit));

        return amountMap.entrySet()
                .stream()
                .map(entry -> entry.getValue().stream()
                        .map(amount -> amount.getQuantity())
                        .collect(Collectors.reducing((b1, b2) -> b1.add(b2)))
                        .map(quantity -> new Amount(entry.getKey(), quantity)))
                .map(amountOptional -> amountOptional.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static Optional<BigInteger> assetBalance(String unit, List<Amount> amounts) {
        return amounts.stream().filter(amount -> unit.equals(amount.getUnit()))
                .findFirst()
                .map(amount -> amount.getQuantity());
    }

    public void waitForTransactionHash(Result<String> result) {
        try {
            if (result.isSuccessful()) { //Wait for transaction to be mined
                int count = 0;
                while (count < 5) {
                    Result<TransactionContent> txnResult = backendService.getTransactionService().getTransaction(result.getValue());
                    if (txnResult.isSuccessful()) {
                        System.out.println(JsonUtil.getPrettyJson(txnResult.getValue().getHash()));
                        break;
                    } else {
                        System.out.println("Waiting for transaction to be processed ....");
                    }

                    count++;
                    Thread.sleep(2000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void topUpFund(String address, long adaAmount) {
        try {
            // URL to the top-up API
            String url = "http://localhost:8080/api/v1/local-cluster/api/addresses/topup";
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            // Set request method to POST
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            // Create JSON payload
            String jsonInputString = String.format("{\"address\": \"%s\", \"adaAmount\": %d}", address, adaAmount);

            // Send the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Check the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Funds topped up successfully.");
            } else {
                System.out.println("Failed to top up funds. Response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
