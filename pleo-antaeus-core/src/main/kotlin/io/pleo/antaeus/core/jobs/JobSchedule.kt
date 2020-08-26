package io.pleo.antaeus.core.jobs
//Service
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService

import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException

//External
import io.pleo.antaeus.core.external.PaymentProvider


// Jobs 
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException //For the retries

//Logger
import org.slf4j.LoggerFactory

class QuartzJob : Job {
    companion object {
        const val INV_ID = "invoiceId"
        const val BILLING = "BillingService"
        const val INVOICE = "invoiceService"
    }
//https://www.programcreek.com/java-api-examples/org.quartz.JobExecutionContext

    override fun execute(execCon : JobExecutionContext) {
        val context = execCon.scheduler.context
        val billingService = context[BILLING] as BillingService
        val invoiceService = context[INVOICE] as InvoiceService
        val logger = LoggerFactory.getLogger(QuartzJob::class.java)

        try {
            val invoice = invoiceService.fetch(execCon.jobDetail.jobDataMap.getIntValue(INV_ID))
            billingService.charge(invoice)
        } catch (CustomerNotFoundException) {
            logger.info("customer not found") 
        } catch (InvoiceNotFoundException) {
            logger.info("invoice can not be found.")
        }
    }
}