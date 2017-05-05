package com.augmentum;

import java.util.*;

import com.chain.api.*;
import com.chain.http.*;
import com.chain.signing.*;

class ControlPrograms {
  public static void main(String[] args) throws Exception {
	  Client client = new Client("http://192.168.222.50:1999",
				"client:cf5193ce3d59609085fe030c147d8eaa23968427078f4252e7598139191ee7b0");
    setup(client);

    // snippet create-receiver
    Receiver aliceReceiver = new Account.ReceiverBuilder()
      .setAccountAlias("alice")
      .create(client);
    String aliceReceiverSerialized = aliceReceiver.toJson();
    // endsnippet

    // snippet build-transaction
    Transaction.Template paymentToReceiver = new Transaction.Builder()
      .addAction(new Transaction.Action.SpendFromAccount()
        .setAccountAlias("bob")
        .setAssetAlias("gold")
        .setAmount(10)
      ).addAction(new Transaction.Action.ControlWithReceiver()
        .setReceiver(Receiver.fromJson(aliceReceiverSerialized))
        .setAssetAlias("gold")
        .setAmount(10)
      ).build(client);

    Transaction.submit(client, HsmSigner.sign(paymentToReceiver));
    // endsnippet

    // snippet retire
    Transaction.Template retirement = new Transaction.Builder()
      .addAction(new Transaction.Action.SpendFromAccount()
        .setAccountAlias("alice")
        .setAssetAlias("gold")
        .setAmount(10)
      ).addAction(new Transaction.Action.Retire()
        .setAssetAlias("gold")
        .setAmount(10)
      ).build(client);

    Transaction.submit(client, HsmSigner.sign(retirement));
    // endsnippet
  }

  public static void setup(Client client) throws Exception {
    MockHsm.Key key = MockHsm.Key.create(client);
    HsmSigner.addKey(key, MockHsm.getSignerClient(client));

    new Asset.Builder()
      .setAlias("gold")
      .addRootXpub(key.xpub)
      .setQuorum(1)
      .create(client);

    new Account.Builder()
      .setAlias("alice")
      .addRootXpub(key.xpub)
      .setQuorum(1)
      .create(client);

    new Account.Builder()
      .setAlias("bob")
      .addRootXpub(key.xpub)
      .setQuorum(1)
      .create(client);

    Transaction.submit(client, HsmSigner.sign(new Transaction.Builder()
      .addAction(new Transaction.Action.Issue()
        .setAssetAlias("gold")
        .setAmount(100)
      ).addAction(new Transaction.Action.ControlWithAccount()
        .setAccountAlias("bob")
        .setAssetAlias("gold")
        .setAmount(100)
      ).build(client)
    ));
  }
}