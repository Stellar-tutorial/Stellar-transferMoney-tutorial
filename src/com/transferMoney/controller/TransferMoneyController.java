package com.transferMoney.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.stellar.sdk.AssetTypeNative;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Memo;
import org.stellar.sdk.Network;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;

@Controller
public class TransferMoneyController {
	
	KeyPair sender;
	KeyPair receiver;
	
	
	
	@RequestMapping("/welcome")
	public String helloWorld() {
 
		return "welcome";
	}
	
	
	
	@RequestMapping("/buttons")
	public ModelAndView buttons() throws IOException {
		sender = createAccount();
		receiver = createAccount();
		
		String senderDetails = displayAccount(sender);
		String receiverDetails = displayAccount(receiver);
		return new ModelAndView("welcome","message","Sender Details- "+
				senderDetails +" \n "+	"Receiver Details- " + receiverDetails);	
	}
	
	@RequestMapping("/sendMoney")
	public ModelAndView sendMoney() throws IOException {
		Network.useTestNetwork();
		Server server = new Server("https://horizon-testnet.stellar.org");

		KeyPair source = KeyPair.fromSecretSeed(sender.getSecretSeed());
		KeyPair destination = KeyPair.fromAccountId(receiver.getAccountId());


		server.accounts().account(destination);

		AccountResponse sourceAccount = server.accounts().account(source);

		Transaction transaction = new Transaction.Builder(sourceAccount)
		        .addOperation(new PaymentOperation.Builder(destination, new AssetTypeNative(), "10").build())
		        .addMemo(Memo.text("Test Transaction"))
		        .build();
		transaction.sign(source);

		SubmitTransactionResponse response = null;
		try {
		  response = server.submitTransaction(transaction);
		  System.out.println(response);
		} catch (Exception e) {
		  System.out.println("Something went wrong!");
		  System.out.println(e.getMessage());
		}
		
		
		String senderDetails = displayAccount(sender);
		String receiverDetails = displayAccount(receiver);
		return new ModelAndView("welcome","message","Sender Details- "+
				senderDetails +" \n "+	"Receiver Details- " + receiverDetails);
	}
	
	@RequestMapping("*")
	public String initPage() {
 
		return "index2";
	}
	
	private String displayAccount(KeyPair accountPair) throws IOException {
		String message = "";
		Server server = new Server("https://horizon-testnet.stellar.org");
		AccountResponse account = server.accounts().account(accountPair);
		for (AccountResponse.Balance balance : account.getBalances()) {
			message = String.format(
		    "Type: %s, Code: %s, Balance: %s",
		    balance.getAssetType(),
		    balance.getAssetCode(),
		    balance.getBalance());
		}
		return message;
	}
	
	private KeyPair createAccount() {
		KeyPair pair = KeyPair.random();
		String friendbotUrl = String.format(
				  "https://friendbot.stellar.org/?addr=%s",
				  pair.getAccountId());
			try {
				InputStream response = new URL(friendbotUrl).openStream();
				String body = new Scanner(response, "UTF-8").useDelimiter("\\A").next();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return pair;
	}

}
