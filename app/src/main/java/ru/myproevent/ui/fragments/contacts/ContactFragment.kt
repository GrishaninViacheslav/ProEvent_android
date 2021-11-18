package ru.myproevent.ui.fragments.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import moxy.MvpView
import moxy.ktx.moxyPresenter
import ru.myproevent.ProEventApp
import ru.myproevent.databinding.FragmentContactBinding
import ru.myproevent.domain.models.entities.Contact
import ru.myproevent.ui.fragments.BaseMvpFragment
import ru.myproevent.ui.presenters.BaseMvpPresenter
import ru.myproevent.ui.presenters.main.RouterProvider

class ContactFragment : BaseMvpFragment(), MvpView {

    private lateinit var contact: Contact

    companion object {
        private const val BUNDLE_CONTACT = "contact"
        fun newInstance(contact: Contact) = ContactFragment().apply {
            arguments = Bundle().apply { putParcelable(BUNDLE_CONTACT, contact) }
        }
    }

    private var _vb: FragmentContactBinding? = null
    private val vb get() = _vb!!

    override val presenter by moxyPresenter {
        BaseMvpPresenter<MvpView>((parentFragment as RouterProvider).router).apply { ProEventApp.instance.appComponent.inject(this) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentContactBinding.inflate(inflater, container, false).also { _vb = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vb.titleButton.setOnClickListener { presenter.onBackPressed() }
        arguments?.getParcelable<Contact>(BUNDLE_CONTACT)?.let { contact = it }
        fillFields()
    }

    private fun fillFields() {
        with(vb) {
            with(contact) {
                titleButton.text =
                    if (!fullName.isNullOrBlank()) {
                        fullName
                    } else if (!nickName.isNullOrBlank()) {
                        nickName
                    } else {
                        "[id: ${userId}]"
                    }
                if(!birthdate.isNullOrBlank()){
                    dateOfBirthTitle.visibility = VISIBLE
                    dateOfBirthValue.visibility = VISIBLE
                    dateOfBirthValue.text = birthdate
                }
                if(!position.isNullOrBlank()){
                    positionTitle.visibility = VISIBLE
                    positionValue.visibility = VISIBLE
                    positionValue.text = position
                }
                if(!msisdn.isNullOrBlank()){
                    phoneTitle.visibility = VISIBLE
                    phoneValue.visibility = VISIBLE
                    phoneValue.text = msisdn
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vb = null
    }
}