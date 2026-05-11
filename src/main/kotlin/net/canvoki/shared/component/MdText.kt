package net.canvoki.shared.component

import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.HtmlCompat
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

@Composable
fun MdText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val annotated =
        remember(markdown) {
            val html = markdown.toHtml()
            val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
            spanned.toAnnotatedString()
        }
    Text(
        text = annotated,
        modifier = modifier,
        style = style,
        color = color,
    )
}

private fun String.toHtml(): String {
    val flavour = CommonMarkFlavourDescriptor()
    val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(this)
    return HtmlGenerator(this, parsedTree, flavour).generateHtml()
}

private fun Spanned.toAnnotatedString() =
    buildAnnotatedString {
        append(this@toAnnotatedString.toString())
        for (span in getSpans(0, length, Any::class.java)) {
            val start = getSpanStart(span)
            val end = getSpanEnd(span)
            if (start < 0 || end > length || start >= end) continue
            when (span) {
                is StyleSpan -> {
                    when (span.style) {
                        android.graphics.Typeface.BOLD ->
                            addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                        android.graphics.Typeface.ITALIC ->
                            addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                        android.graphics.Typeface.BOLD_ITALIC -> {
                            addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                            addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                        }
                    }
                }
                is UnderlineSpan ->
                    addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                is StrikethroughSpan ->
                    addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, end)
                is URLSpan ->
                    addStyle(
                        SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
                        start,
                        end,
                    )
            }
        }
    }
