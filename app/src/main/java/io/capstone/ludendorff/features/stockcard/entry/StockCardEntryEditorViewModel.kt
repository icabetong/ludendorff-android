package io.capstone.ludendorff.features.stockcard.entry

import io.capstone.ludendorff.features.shared.BaseViewModel

class StockCardEntryEditorViewModel: BaseViewModel() {

    var stockCardEntry = StockCardEntry()

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
}