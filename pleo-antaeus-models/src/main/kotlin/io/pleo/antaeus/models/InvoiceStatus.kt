package io.pleo.antaeus.models

enum class InvoiceStatus {
    PENDING,
    PAID,
    NETWORK_ERROR,
    NOT_KNOWN,
    CURRENCY_MISMATCH,
    CUSTOMER_NOT_FOUND,
    INSUFFICIENT_BALANCE
}
