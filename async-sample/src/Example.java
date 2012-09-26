import org.proofcafe.async.Async;
import org.proofcafe.async.AsyncCancelledException;
import org.proofcafe.async.Background;

import android.util.Log;

public class Example {

	public static void main(String[] args) {

		// build a task, in a 'compositional' manner
		Async<Unit> task = fetchHtml // first, fetch html in a background
										// thread..
		// then process it in the UI thread
		.new BindInUiThread<Unit>() {
			@Override
			protected Async<Unit> f(String html) throws AsyncCancelledException {

				// we are in UI thread!

				/* put html string into some cool UI ... */
				/* textView.setText(html); */

				/*
				 * and then, send it as an email to somewhere, again in the
				 * background.
				 */
				return new SendMail(html);
			}
		}.get();

		// actually run the task
		task.exec(null); // currently, this null arg is meaningless :)
	}

	static Async<String> fetchHtml = new Background<String, Void>() {
		protected String doInBackground() throws Exception {

			// here we are in a background thread!!

			String html;

			/* fetch html from somewhere ... */
			html = "<p>some html</p>";

			return html;
		}
	};

	static class SendMail extends Background<Unit, Void> {
		private final String mailBody;

		SendMail(String mailBody) {
			this.mailBody = mailBody;
		}

		protected Unit doInBackground() throws Exception {

			// background!

			/* send mail to somewhere */
			/* SMTP.send("keigo.imai@gmail.com", mailBody); */
			
			Log.d("sample", "mail sent:" + mailBody);

			return Unit.tt;
		}

	}

}
