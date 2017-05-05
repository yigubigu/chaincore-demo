package com.velo;
import java.net.URL;

import com.chain.api.Account;
import com.chain.api.Asset;
import com.chain.api.MockHsm;
import com.chain.api.Transaction;
import com.chain.http.Client;
import com.chain.signing.HsmSigner;

class Keys {
  public static void main(String[] args) throws Exception {
	  
	 try { 
    Client client = new Client("http://192.168.222.50:1999", "client:cf5193ce3d59609085fe030c147d8eaa23968427078f4252e7598139191ee7b0");
    
    
    // snippet create-key
    
    MockHsm.Key key = MockHsm.Key.create(client);
    // endsnippet

    // snippet signer-add-key
    HsmSigner.addKey(key, MockHsm.getSignerClient(client));
    // endsnippet

    new Asset.Builder()
      .setAlias("point")
      .addRootXpub(key.xpub)
      .setQuorum(1)
      .create(client);

    new Account.Builder()
      .setAlias("alice")
      .addRootXpub(key.xpub)
      .setQuorum(1)
      .create(client);

    Transaction.Template unsigned = new Transaction.Builder()
      .addAction(new Transaction.Action.Issue()
        .setAssetAlias("gold")
        .setAmount(100)
      ).addAction(new Transaction.Action.ControlWithAccount()
        .setAccountAlias("alice")
        .setAssetAlias("gold")
        .setAmount(100)
      ).build(client);

    // snippet sign-transaction
    Transaction.Template signed = HsmSigner.sign(unsigned);
    // endsnippet
	 }catch (Exception ex) {
		 System.out.println(ex.getMessage());
	 }
  }
}