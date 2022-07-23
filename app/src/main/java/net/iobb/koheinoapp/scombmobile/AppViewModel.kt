package net.iobb.koheinoapp.scombmobile

import androidx.lifecycle.ViewModel

const val SCOMB_LOGIN_PAGE_URL = "https://scombz.shibaura-it.ac.jp/saml/login?idp=http://adfs.sic.shibaura-it.ac.jp/adfs/services/trust"
const val SCOMB_HOME_URL = "https://scombz.shibaura-it.ac.jp/portal/home"
const val TWO_STEP_VERIFICATION_LOGIN_BUTTON_ID = "continueButton"

class AppViewModel : ViewModel(){

    var sessionId: String? = null
    var userId: String? = null
    var password: String? = null
}