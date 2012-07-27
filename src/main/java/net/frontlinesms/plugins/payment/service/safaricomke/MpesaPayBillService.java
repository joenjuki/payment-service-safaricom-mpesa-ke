package net.frontlinesms.plugins.payment.service.safaricomke;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.frontlinesms.data.domain.FrontlineMessage;
import net.frontlinesms.plugins.payment.service.PaymentServiceException;
import net.frontlinesms.plugins.payment.service.ui.PaymentServiceUiActionHandler;
import net.frontlinesms.serviceconfig.ConfigurableServiceProperties;
import net.frontlinesms.ui.UiGeneratorController;

import org.creditsms.plugins.paymentview.data.domain.Account;
import org.creditsms.plugins.paymentview.data.domain.OutgoingPayment;

@ConfigurableServiceProperties(name="MPESA Kenya PayBill", icon="/icons/mpesa_ke_paybill.png")
public class MpesaPayBillService extends MpesaPaymentService {
	private static final String PAYBILL_REGEX = "[A-Z0-9]+ Confirmed.\\s+"
			+ "on (([1-2]?[1-9]|[1-2]0|3[0-1])/([1-9]|1[0-2])/(1[1-3])) at ([1]?\\d:[0-5]\\d) (AM|PM)\\s+"
			+ "Ksh[,|\\d]+(|.[\\d]{2}) received from ([A-Za-z ]+) 2547[\\d]{8}.\\s+"
			+ "Account Number (\\d|[A-Za-z0-9]|[A-Za-z])+\\s+"
			+ "New Utility balance is Ksh[,|\\d]+(|.[\\d]{2})";	
	
	private static final String BALANCE_REGEX = 
		"[A-Z0-9]+ Confirmed.\n"
		+ "on (([1-2]?[1-9]|[1-2]0|3[0-1])/([1-9]|1[0-2])/(1[1-3])) at ([1]?\\d:[0-5]\\d) (AM|PM)\n"
		+ "Ksh[,|\\d]+ received from ([A-Za-z ]+) 2547[\\d]{8}.\n"
		+ "Account Number (\\[A-Za-z]+|\\d+|[A-Za-z0-9]+)\n"
		+ "New Utility balance is Ksh[,|\\d]+\n";
	
	public boolean isOutgoingPaymentEnabled() {
		return super.isOutgoingPaymentEnabled();
	}
	
	@Override
	protected boolean isValidBalanceMessage(FrontlineMessage message) {
		return message.getTextContent().matches(BALANCE_REGEX);
	}
	
	@Override
	protected void processBalance(FrontlineMessage message){
	}
	
	@Override
	Account getAccount(FrontlineMessage message) {
		String accNumber = getFirstMatch(message, "Account Number [A-Z0-9]*");
		return accountDao.getAccountByAccountNumber(accNumber
				.substring("Account Number ".length()));
	}

	@Override
	String getPaymentBy(FrontlineMessage message) {
		try {
			String nameAndPhone = getFirstMatch(message, RECEIVED_FROM +" "+PAID_BY_PATTERN + " " + PHONE_PATTERN);
			String nameWKsh = nameAndPhone.replace(RECEIVED_FROM, "");
			String names = getFirstMatch(nameWKsh, PAID_BY_PATTERN).trim();
			return names;
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	Date getTimePaid(FrontlineMessage message) {
		String longtext = message.getTextContent().replace("\\s", " ");
		String section1 = longtext.split("on ")[1];
		String datetimesection = section1.split(AMOUNT_PATTERN+ " " + RECEIVED_FROM)[0];
		String datetime = datetimesection.replace(" at ", " ");

		Date date = null;
		try {
			date = new SimpleDateFormat(DATETIME_PATTERN).parse(datetime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	@Override
	boolean isMessageTextValid(String messageText) {
		return messageText.matches(PAYBILL_REGEX);
	}
	
	public void makePayment(OutgoingPayment op) throws PaymentServiceException {
		throw new PaymentServiceException("Making payments is not possible with a PayBill account.");
	}
	
	public PaymentServiceUiActionHandler getServiceActionUiHandler(UiGeneratorController ui) {
		return null;
	}

	@Override
	public String toString() {
		return "M-PESA Kenya: Paybill Service";
	}
}
