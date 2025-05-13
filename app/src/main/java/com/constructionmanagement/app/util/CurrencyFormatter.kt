package com.constructionmanagement.app.util

import java.text.NumberFormat
import java.util.*

/**
 * Utility class for formatting currency values
 */
object CurrencyFormatter {
    
    /**
     * Formats a number as Indian Rupees
     * @param amount The amount to format
     * @return Formatted string with the Rupee symbol
     */
    fun formatRupees(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        val formattedAmount = format.format(amount)
        
        // The NumberFormat adds the currency symbol, but we want to use our custom symbol
        // So we remove the first character (₹) and return just the formatted number
        return formattedAmount.substring(1)
    }
}
