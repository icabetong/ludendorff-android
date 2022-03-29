package io.capstone.ludendorff.features.stockcard.entry

import io.capstone.ludendorff.features.shared.BaseViewModel

class StockCardEntryEditorViewModel: BaseViewModel() {

    var stockCardEntry = StockCardEntry()

    fun triggerReferenceChanged(reference: String) {
        stockCardEntry.reference = reference
    }
    fun triggerReceiptQuantityChanged(receiptQuantity: String) {
        if (receiptQuantity.isNotBlank()) {
            stockCardEntry.receiptQuantity = receiptQuantity.toInt()
        } else stockCardEntry.receiptQuantity = 0
    }
    fun triggerRequestedQuantityChanged(requestQuantity: String) {
        if (requestQuantity.isNotBlank()) {
            stockCardEntry.requestedQuantity = requestQuantity.toInt()
        } else stockCardEntry.requestedQuantity = 0
    }
    fun triggerIssueQuantityChanged(issueQuantity: String) {
        if (issueQuantity.isNotBlank()) {
            stockCardEntry.issueQuantity = issueQuantity.toInt()
        } else stockCardEntry.issueQuantity = 0
    }
    fun triggerIssueOffice(issueOffice: String) {
        stockCardEntry.issueOffice = issueOffice
    }
    fun triggerBalanceQuantity(balanceQuantity: String) {
        if (balanceQuantity.isNotBlank())
            stockCardEntry.balanceQuantity = balanceQuantity.toInt()
        else stockCardEntry.balanceQuantity = 0
    }
    fun triggerBalanceTotalPrice(totalPrice: String) {
        if (totalPrice.isNotBlank())
            stockCardEntry.balanceTotalPrice = totalPrice.toDouble()
        else stockCardEntry.balanceTotalPrice = 0.0
    }
}