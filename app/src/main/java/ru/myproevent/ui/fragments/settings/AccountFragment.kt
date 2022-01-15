package ru.myproevent.ui.fragments.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.SpannableStringBuilder
import android.text.method.KeyListener
import android.view.View
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE
import com.theartofdev.edmodo.cropper.CropImage
import moxy.ktx.moxyPresenter
import ru.myproevent.ProEventApp
import ru.myproevent.R
import ru.myproevent.databinding.FragmentAccountBinding
import ru.myproevent.domain.models.ProfileDto
import ru.myproevent.ui.fragments.BaseMvpFragment
import ru.myproevent.ui.presenters.main.RouterProvider
import ru.myproevent.ui.presenters.settings.account.AccountPresenter
import ru.myproevent.ui.presenters.settings.account.AccountView
import ru.myproevent.ui.views.KeyboardAwareTextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class AccountFragment : BaseMvpFragment<FragmentAccountBinding>(FragmentAccountBinding::inflate),
    AccountView {

    val calendar: Calendar = Calendar.getInstance()
    var currYear: Int = calendar.get(Calendar.YEAR)
    var currMonth: Int = calendar.get(Calendar.MONTH)
    var currDay: Int = calendar.get(Calendar.DAY_OF_MONTH)

    private val DateEditClickListener = View.OnClickListener {
        // TODO: отрефакторить
        // https://github.com/terrakok/Cicerone/issues/106
        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
        val prev: Fragment? = parentFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        var pickerYear = currYear
        var pickerMonth = currMonth
        var pickerDay = currDay
        if (!binding.dateOfBirthEdit.text.isNullOrEmpty()) {
            val pickerDate = GregorianCalendar().apply {
                time =
                    SimpleDateFormat(getString(R.string.dateFormat)).parse(binding.dateOfBirthEdit.text.toString())
            }
            pickerYear = pickerDate.get(Calendar.YEAR)
            pickerMonth = pickerDate.get(Calendar.MONTH)
            pickerDay = pickerDate.get(Calendar.DATE)
        }
        val newFragment: DialogFragment =
            ProEventDatePickerDialog.newInstance(pickerYear, pickerMonth, pickerDay).apply {
                onDateSetListener = { year, month, dayOfMonth ->
                    val gregorianCalendar = GregorianCalendar(
                        year, month, dayOfMonth
                    )
                    this@AccountFragment.binding.dateOfBirthEdit.text = SpannableStringBuilder(
                        // TODO: для вывода сделать local date format
                        SimpleDateFormat(getString(R.string.dateFormat)).apply {
                            calendar = gregorianCalendar
                        }.format(
                            gregorianCalendar.time
                        )
                    )
                }
            }
        newFragment.show(ft, "dialog")
    }

    private fun showKeyBoard(view: View) {
        val imm: InputMethodManager =
            requireContext().getSystemService(InputMethodManager::class.java)
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    override val presenter by moxyPresenter {
        AccountPresenter((parentFragment as RouterProvider).router).apply {
            ProEventApp.instance.appComponent.inject(this)
        }
    }

    private lateinit var defaultKeyListener: KeyListener
    private lateinit var phoneKeyListener: KeyListener

    private fun setEditListeners(
        textInput: TextInputLayout,
        textEdit: KeyboardAwareTextInputEditText
    ) {
        textEdit.keyListener = null
        textInput.setEndIconOnClickListener {
            textEdit.keyListener = defaultKeyListener
            textEdit.requestFocus()
            showKeyBoard(textEdit)
            textEdit.text?.let { it1 -> textEdit.setSelection(it1.length) }
            textInput.endIconMode = END_ICON_NONE
            binding.save.visibility = VISIBLE
        }
    }

    private fun setPhoneListeners(
        textInput: TextInputLayout,
        textEdit: KeyboardAwareTextInputEditText
    ) {
        textEdit.keyListener = null
        textInput.setEndIconOnClickListener {
            textEdit.keyListener = phoneKeyListener
            textEdit.requestFocus()
            showKeyBoard(textEdit)
            textEdit.text?.let { it1 -> textEdit.setSelection(it1.length) }
            textInput.endIconMode = END_ICON_NONE
            binding.save.visibility = VISIBLE
        }
    }

    companion object {
        fun newInstance() = AccountFragment()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        defaultKeyListener = nameEdit.keyListener
        setEditListeners(nameInput, nameEdit)
        phoneKeyListener = phoneEdit.keyListener
        setPhoneListeners(phoneInput, phoneEdit)
        dateOfBirthEdit.keyListener = null
        dateOfBirthEdit.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                dateOfBirthEdit.performClick()
            }
        }
        dateOfBirthInput.setEndIconOnClickListener {
            dateOfBirthEdit.requestFocus()
            dateOfBirthEdit.setOnClickListener(DateEditClickListener)
            dateOfBirthEdit.performClick()
            dateOfBirthInput.endIconMode = END_ICON_NONE
            save.visibility = VISIBLE
        }
        setEditListeners(positionInput, positionEdit)
        setEditListeners(roleInput, roleEdit)
        save.setOnClickListener {
            presenter.saveProfile(
                nameEdit.text.toString(),
                phoneEdit.text.toString(),
                dateOfBirthEdit.text.toString(),
                positionEdit.text.toString(),
                roleEdit.text.toString()
            )
        }
        titleButton.setOnClickListener { presenter.onBackPressed() }
        phoneEdit.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        runImageCropper.setOnClickListener { presenter.loadImageFragment() }
        phoneEdit.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (phoneEdit.text.isNullOrEmpty()) {
                    phoneEdit.text = SpannableStringBuilder("+7")
                }
            }
        }

        presenter.getProfile()
    }

    override fun showProfile(profileDto: ProfileDto) {
        with(binding) {
            with(profileDto) {
                fullName?.let { nameEdit.text = SpannableStringBuilder(it) }
                msisdn?.let { phoneEdit.text = SpannableStringBuilder(it) }
                birthdate?.let { dateOfBirthEdit.text = SpannableStringBuilder(it) }
                position?.let { positionEdit.text = SpannableStringBuilder(it) }
                description?.let { roleEdit.text = SpannableStringBuilder(it) }
            }
        }
    }

    override fun makeProfileEditable() {
        // TODO:
        showMessage("makeProfileEditable()")
    }
}