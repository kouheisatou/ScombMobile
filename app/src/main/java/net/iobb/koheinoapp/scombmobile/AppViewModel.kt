package net.iobb.koheinoapp.scombmobile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

const val SCOMB_LOGIN_PAGE_URL = "https://scombz.shibaura-it.ac.jp/saml/login?idp=http://adfs.sic.shibaura-it.ac.jp/adfs/services/trust"
const val SCOMB_HOME_URL = "https://scombz.shibaura-it.ac.jp/portal/home"
const val SCOMB_LOGGED_OUT_PAGE_URL = "https://scombz.shibaura-it.ac.jp/login"

// Scombに2段階認証が未設定でログインしようとすると出る"2要素認証は無効になっています。"確認画面の"次へ"ボタンのhtmlのid
const val TWO_STEP_VERIFICATION_LOGIN_BUTTON_ID = "continueButton"

class AppViewModel : ViewModel(){

    var sessionId: String? = null
    var userId = MutableLiveData("")
}