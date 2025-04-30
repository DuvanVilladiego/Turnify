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

    private lateinit var txtTitle: TextView
    private lateinit var txtDescription: TextView
    private lateinit var btnMain: Button
    private lateinit var cnstLayout: ConstraintLayout

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

        cnstLayout = view.findViewById(R.id.cnstLayoutSimpleButtonText)
        txtTitle = view.findViewById(R.id.txtTitle)
        txtDescription = view.findViewById(R.id.txtDescription)
        btnMain = view.findViewById(R.id.btnMain)

        // Mostrar u ocultar el CardView según la propiedad
        cnstLayout.visibility = if (isShow) View.VISIBLE else View.GONE

        // Setear los textos y visibilidades
        if (title.isNullOrEmpty()) {
            txtTitle.visibility = View.GONE
        } else {
            txtTitle.text = title
        }

        if (description.isNullOrEmpty()) {
            txtDescription.visibility = View.GONE
        } else {
            txtDescription.text = description
        }

        if (btnText.isNullOrEmpty()) {
            btnMain.visibility = View.GONE
        } else {
            btnMain.text = btnText
        }

        // Acción del botón
        btnMain.setOnClickListener {
            cnstLayout.visibility = View.GONE
        }
    }

    class Builder {
        private val args = Bundle()

        init {
            args.putBoolean(ARG_SHOW, true)
        }

        fun isTitle(title: String) = apply { args.putString(ARG_TITLE, title) }
        fun isDescription(description: String) = apply { args.putString(ARG_DESCRIPTION, description) }
        fun isMainButton(text: String) = apply { args.putString(ARG_BTN_TEXT, text) }
        fun isShow(show: Boolean) = apply { args.putBoolean(ARG_SHOW, show) }

        fun build(): SimpleTextButton {
            val fragment = SimpleTextButton()
            fragment.arguments = args
            return fragment
        }
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESCRIPTION = "arg_description"
        private const val ARG_BTN_TEXT = "arg_text"
        private const val ARG_SHOW = "arg_show"

        @JvmStatic
        fun newInstance(title: String, description: String, textButton: String, isShow: Boolean) =
            SimpleTextButton().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_DESCRIPTION, description)
                    putString(ARG_BTN_TEXT, textButton)
                    putBoolean(ARG_SHOW, isShow)
                }
            }
    }
}
