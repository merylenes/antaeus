package io.pleo.antaeus.core.services
//Execeptions
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException

//External
import io.pleo.antaeus.core.external.PaymentProvider
// Models
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {

    private fun charge(invoice: Invoice): InvStatus {
        return try {
                    if (paymentProvider.charge(invoice)) {
                        InvStatus.PAID
                    }
                    else { InvStatus.INSUFFICIENT_BALANCE
                    }

                    } catch (e: CustomerNotFoundException) {
                        InvStatus.CUSTOMER_NOT_FOUND
                        
                    } catch (e: CurrencyMismatchException) {
                        InvStatus.CURRENCY_MISMATCH

                    } catch (e: NetworkException) {
                        InvStatus.NETWORK_ERROR

                    } catch (e: Exception) {
                        InvStatus.NOT_KNOWN
                    }
                }
               }
    

    fun chargeSingleInvoice(invoice: Invoice): Boolean {

        val status = charge(invoice)
        val success = status == InvoiceStatus.PAID
        if(success) {
            invoiceService.update(invoice)
        }
        return success
    }


    fun chargeInvoices(): {
        // Get all invoices
        val invoices = invoiceService.fetchAllByStatus(InvoiceStatus.UNPAID)

        if (invoices.isNotEmpty()) {
        
            for (invoice in invoices) {
                if chargeSingleInvoice(invoice) {
                    logger.info {
                        "Charged customer: ${invoice.customerId} " +
                        "for invoice: ${invoice.id} "
                                }
                }
            } 
        }
    }



}
