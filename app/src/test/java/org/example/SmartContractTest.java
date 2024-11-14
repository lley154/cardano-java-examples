package org.example;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.api.UtxoSupplier;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.KupmiosBackendService;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.DefaultUtxoSupplier;
import com.bloxbean.cardano.client.common.model.Networks;
import static com.bloxbean.cardano.client.common.CardanoConstants.LOVELACE;
import com.bloxbean.cardano.client.function.helper.ScriptUtxoFinders;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.plutus.blueprint.PlutusBlueprintUtil;
import com.bloxbean.cardano.client.plutus.blueprint.model.PlutusVersion;
import com.bloxbean.cardano.client.plutus.spec.BytesPlutusData;
import com.bloxbean.cardano.client.plutus.spec.ConstrPlutusData;
import com.bloxbean.cardano.client.plutus.spec.PlutusData;
import com.bloxbean.cardano.client.plutus.spec.PlutusScript;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.ScriptTx;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import java.math.BigInteger;

/* Aiken Smart Contract

use aiken/collection/list
use aiken/crypto.{VerificationKeyHash}
use cardano/transaction.{OutputReference, Transaction}
 
pub type Datum {
  owner: VerificationKeyHash,
}
 
pub type Redeemer {
  msg: ByteArray,
}
 
validator hello_world {
  spend(
    datum: Option<Datum>,
    redeemer: Redeemer,
    _own_ref: OutputReference,
    self: Transaction,
  ) {
    expect Some(Datum { owner }) = datum
    let must_say_hello = redeemer.msg == "Hello, World!"
    let must_be_signed = list.has(self.extra_signatories, owner)
    must_say_hello && must_be_signed
  }
}
*/

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)   
public class SmartContractTest {
        
    private static String senderMnemonic = "flush together outer effort tenant photo waste distance rib grocery aunt broken weather arrow jungle debris finger flee casino doctor group echo baby near";
    private static Account senderAccount = new Account(Networks.testnet(), senderMnemonic);
    private static String receiverMnemonic = "essence pilot click armor alpha noise mixture soldier able advice multiply inject ticket pride airport uncover honey desert curtain sun true toast valve half";
    private static Account receiverAccount = new Account(Networks.testnet(), receiverMnemonic);
    private static String compiledCode = "590169010100323232323232323225333002323232323253330073370e900118049baa0011323232533300a3370e900018061baa005132533300f00116132533333301300116161616132533301130130031533300d3370e900018079baa004132533300e3371e6eb8c04cc044dd5004a4410d48656c6c6f2c20576f726c642100100114a06644646600200200644a66602a00229404c94ccc048cdc79bae301700200414a2266006006002602e0026eb0c048c04cc04cc04cc04cc04cc04cc04cc04cc040dd50051bae301230103754602460206ea801054cc03924012465787065637420536f6d6528446174756d207b206f776e6572207d29203d20646174756d001616375c0026020002601a6ea801458c038c03c008c034004c028dd50008b1805980600118050009805001180400098029baa001149854cc00d2411856616c696461746f722072657475726e65642066616c736500136565734ae7155ceaab9e5573eae855d12ba401";
    private static PlutusScript plutusScript = PlutusBlueprintUtil.getPlutusScriptFromCompiledCode(compiledCode, PlutusVersion.v3);
    private static String scriptAddress = AddressProvider.getEntAddress(plutusScript, Networks.testnet()).toBech32();
    private static TestHelper testHelper;
    private static BigInteger scriptBalanceBefore;
    private static BigInteger senderBalanceBefore;
    private static BigInteger receiverBalanceBefore;
    private static Amount adaAmount = Amount.ada(10);
    private static BackendService backendService;

    @BeforeAll
    static void setup() {

        backendService = new KupmiosBackendService("http://localhost:1337", "http://localhost:1442");
        testHelper = new TestHelper(backendService);
    }

    @Test
    @Order(1)
    void lock_lovelace() throws Exception {

        System.out.println("================================================");
        System.out.println("Test lock_lovelace");
        String senderAddress = senderAccount.baseAddress();
        log.info("Sender address : " + senderAddress);
        senderBalanceBefore = testHelper.lovelaceBalance(senderAddress);
        System.out.println("sender balance before: " + senderBalanceBefore);

        log.info("Script address : " + scriptAddress);
        scriptBalanceBefore = testHelper.assetBalance(scriptAddress, LOVELACE);
        System.out.println("script balance before: " + scriptBalanceBefore);

        // Create datum
        PlutusData datum = ConstrPlutusData.of(0, BytesPlutusData.of(receiverAccount.getBaseAddress().getPaymentCredentialHash().get()));

        // Build transaction
        Tx tx = new Tx()
                .payToContract(scriptAddress, adaAmount, datum)
                .from(senderAccount.baseAddress());
        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        
        // Sign and submit transaction
        Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(senderAccount))
                .completeAndWait(System.out::println);
        
        System.out.println("Transaction result: " + JsonUtil.getPrettyJson(result));
        System.out.println("Sender address after: " + testHelper.assetBalance(senderAddress, LOVELACE));
        System.out.println("Script address after: " + testHelper.assetBalance(scriptAddress, LOVELACE));

        // Add assertion to verify the transaction
        BigInteger scriptBalanceAfter = testHelper.assetBalance(scriptAddress, LOVELACE);
        assert scriptBalanceAfter.equals(scriptBalanceBefore.add(adaAmount.getQuantity())) : 
            "Expected receiver balance to be " + scriptBalanceBefore.add(adaAmount.getQuantity()) + " ADA, but was: " + scriptBalanceAfter;

    }

    @Test
    @Order(2)
    void unlock_lovelace() throws Exception {

        System.out.println("================================================");
        System.out.println("Test unlock_lovelace");
        String recieverAddress = receiverAccount.baseAddress();
        log.info("Receiver address : " + recieverAddress);
        receiverBalanceBefore = testHelper.lovelaceBalance(recieverAddress);
        System.out.println("Receiver balance before: " + receiverBalanceBefore);

        log.info("Script address : " + scriptAddress);
        scriptBalanceBefore = testHelper.assetBalance(scriptAddress, LOVELACE);
        System.out.println("script balance before: " + scriptBalanceBefore);

        PlutusData datum = ConstrPlutusData.of(0, BytesPlutusData.of(receiverAccount.getBaseAddress().getPaymentCredentialHash().get()));

        UtxoSupplier utxoSupplier = new DefaultUtxoSupplier(backendService.getUtxoService());
        Utxo scriptUtxo = ScriptUtxoFinders.findFirstByInlineDatum(utxoSupplier, scriptAddress, datum).orElseThrow();

        // Create redeemer
        PlutusData redeemer = ConstrPlutusData.of(0, BytesPlutusData.of("Hello, World!"));

        // Build transaction
        ScriptTx sctipTx = new ScriptTx()
                .collectFrom(scriptUtxo, redeemer)
                .payToAddress(receiverAccount.baseAddress(), adaAmount)
                .attachSpendingValidator(plutusScript);
        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        
        // Sign and submit transaction
        Result<String> result = quickTxBuilder.compose(sctipTx)
                .feePayer(receiverAccount.baseAddress())
                .collateralPayer(receiverAccount.baseAddress())
                .withSigner(SignerProviders.signerFrom(receiverAccount))
                .withRequiredSigners(receiverAccount.getBaseAddress())
                .completeAndWait(System.out::println);

        System.out.println("Transaction result: " + JsonUtil.getPrettyJson(result));
        System.out.println("Receiver address after: " + testHelper.assetBalance(recieverAddress, LOVELACE));
        System.out.println("Script address after: " + testHelper.assetBalance(scriptAddress, LOVELACE));

        // Add assertion to verify the transaction
        BigInteger scriptBalanceAfter = testHelper.assetBalance(scriptAddress, LOVELACE);
        assert scriptBalanceAfter.equals(scriptBalanceBefore.subtract(adaAmount.getQuantity())) : 
            "Expected script balance to be " + scriptBalanceBefore.subtract(adaAmount.getQuantity()) + " ADA, but was: " + scriptBalanceAfter;

    }
}