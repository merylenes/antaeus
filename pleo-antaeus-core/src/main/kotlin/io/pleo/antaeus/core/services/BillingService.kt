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
import io.pleo.antaeus.core.jobs.QuartzJob

//Logger
//import mu.KotlinLogging
import org.slf4j.LoggerFactory
// Scheduler
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.CronScheduleBuilder
import org.quartz.impl.StdSchedulerFactory

//private val logger = KotlinLogging.logger {}
//public val logger = LoggerFactory.getILoggerFactory().getLogger("Invoice_Billing")

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService,
    private val invoice: Invoice,
    val scheduler: Scheduler = StdSchedulerFactory().scheduler) {
        private val logger = LoggerFactory.getLogger(BillingService::class.java)

 //Scheduler, it needs to be instantiated    

        public fun charge(invoice: Invoice) {
            chargeInvoice(invoice, paymentProvider, invoiceService) 
        }

           fun chargeInvoice(invoice: Invoice, paymentProvider: PaymentProvider, invoiceService: InvoiceService) {
          
            if (invoice.status == InvoiceStatus.PENDING) {
                if (paymentProvider.charge(invoice)) {
                    invoiceService.update(invoice)
                    invoiceService.markInvoiceAsPaid(invoice)
                } 
            } else {
                logger.info("Invoice processing halted")
            }
        }
        // This fuction is called from pleo-antaeus-app\src\main\kotlin\io\pleo\antaeus\app\utils.kt
        fun monthlyInvoiceRun(invoice: Invoice, cron: String) {
              // Tell quartz to schedule the job using our trigger
              //sched.scheduleJob(job, trigger);
            scheduler.scheduleJob(simpleJobDetail(invoice), cronTrigger(invoice, cron))
        }
        
        //https://www.baeldung.com/quartz
        //CronTrigger trigger = TriggerBuilder.newTrigger()
        //.withIdentity("trigger3", "group1")
        //.withSchedule(CronScheduleBuilder.cronSchedule("0 0/2 8-17 * * ?"))
        //.forJob("myJob", "group1")
        //.build();
        //https://github.com/archer920/scheduling-tasks/blob/master/src/main/kotlin/com/stonesoupprogramming/schedulingtasks/SchedulingTasks.kt
        //http://www.quartz-scheduler.org/documentation/2.4.0-SNAPSHOT/tutorials/tutorial-lesson-06.html
        private fun cronTrigger(invoice: Invoice, cron: String) = TriggerBuilder.newTrigger()
                .withIdentity("trigger invoice#" + invoice.id + " " + invoice.amount,"Monthly_Run_Last_Day")
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build()

        //JobDetail job = JobBuilder.newJob(SimpleJob.class)
        //.withIdentity("myJob", "group1")
        //.build();
        private fun simpleJobDetail(invoice: Invoice) = JobBuilder
                .newJob(QuartzJob.class)
                .usingJobData(QuartzJob.INV_ID, invoice.id)
                .build()
    }