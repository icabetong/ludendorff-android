package io.capstone.ludendorff.components.utils

import java.security.SecureRandom

class PasswordManager private constructor() {

    companion object {
        fun generateRandom(isWithLetters: Boolean,
                           isWithUppercase: Boolean,
                           isWithNumbers: Boolean,
                           isWithSpecial: Boolean,
                           length: Int) : String {
            return PasswordManager().generatePassword(isWithLetters, isWithUppercase,
                isWithNumbers, isWithSpecial, length)
        }
    }

    private val letters : String = "abcdefghijklmnopqrstuvwxyz"
    private val uppercaseLetters : String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val numbers : String = "0123456789"
    private val special : String = "@#=+!£$%&?"
    private val maxPasswordLength : Float = 20F //Max password lenght that my app creates
    private val maxPasswordFactor : Float = 10F

    /**
     * Generate a random password
     * @param isWithLetters Boolean value to specify if the password must contain letters
     * @param isWithUppercase Boolean value to specify if the password must contain uppercase letters
     * @param isWithNumbers Boolean value to specify if the password must contain numbers
     * @param isWithSpecial Boolean value to specify if the password must contain special chars
     * @param length Int value with the length of the password
     * @return the new password.
     */
    fun generatePassword(isWithLetters: Boolean,
                         isWithUppercase: Boolean,
                         isWithNumbers: Boolean,
                         isWithSpecial: Boolean,
                         length: Int) : String {

        var result = ""
        var i = 0

        if(isWithLetters){ result += this.letters }
        if(isWithUppercase){ result += this.uppercaseLetters }
        if(isWithNumbers){ result += this.numbers }
        if(isWithSpecial){ result += this.special }

        val rnd = SecureRandom.getInstance("SHA1PRNG")
        val sb = StringBuilder(length)

        while (i < length) {
            val randomInt : Int = rnd.nextInt(result.length)
            sb.append(result[randomInt])
            i++
        }

        return sb.toString()
    }

}