package com.turnify.app.fragments.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.turnify.app.R

class SimpleTextButton : Fragment() {

    private var title: String? = null
    private var description: String? = null
    private var btnText: String? = null
    private var isShow: Boolean = false
    private var onClick: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE)
            description = it.getString(ARG_DESCRIPTION)
            btnText = it.getString(ARG_BTN_TEXT)
            isShow = it.getBoolean(ARG_SHOW)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_simple_text_button, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cnstLayout = view.findViewById<ConstraintLayout>(R.id.cnstLayoutSimpleButtonText)
        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val txtDescription = view.findViewById<TextView>(R.id.txtDescription)
        val btnMain = view.findViewById<Button>(R.id.btnMain)

        cnstLayout.visibility = if (isShow) View.VISIBLE else View.GONE

        txtTitle.apply {
            if (title.isNullOrEmpty()) visibility = View.GONE
            else text = title
        }

        txtDescription.apply {
            if (description.isNullOrEmpty()) visibility = View.GONE
            else text = description
        }

        if (!btnText.isNullOrEmpty()) {
            btnMain.text = btnText
            btnMain.setOnClickListener {
                onClick?.invoke() // Usa el callback personalizado si existe
                parentFragmentManager.popBackStack() // Cierra el fragmento
            }
        } else {
            btnMain.visibility = View.GONE
        }
    }

    // Permite al Builder pasar el callback
    fun setOnClickListener(listener: () -> Unit) {
        onClick = listener
    }

    class Builder {
        private val args = Bundle()
        private var onClick: (() -> Unit)? = null

        init {
            args.putBoolean(ARG_SHOW, true)
        }

        fun isTitle(title: String) = apply { args.putString(ARG_TITLE, title) }
        fun isDescription(description: String) = apply { args.putString(ARG_DESCRIPTION, description) }
        fun isMainButton(text: String) = apply { args.putString(ARG_BTN_TEXT, text) }
        fun isShow(show: Boolean) = apply { args.putBoolean(ARG_SHOW, show) }

        fun setButtonClickListener(listener: () -> Unit) = apply {
            onClick = listener
        }

        fun build(): SimpleTextButton {
            val fragment = SimpleTextButton()
            fragment.arguments = args
            fragment.setOnClickListener(onClick ?: {}) // Asigna el callback si no es nulo
            return fragment
        }
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESCRIPTION = "arg_description"
        private const val ARG_BTN_TEXT = "arg_text"
        private const val ARG_SHOW = "arg_show"
    }
}
