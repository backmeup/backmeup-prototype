package org.backmeup.logic.impl.helper;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.FormField;
import net.htmlparser.jericho.FormFields;
import net.htmlparser.jericho.Source;

public class AuthenticationPerformer {
  public static String performAuthentication(String url,
      AutomaticAuthorization authorizer) {
    try {
      CookieHandler
          .setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
      URI authUrl;
      authUrl = new URI(url);
      HttpURLConnection conn = (HttpURLConnection) authUrl.toURL()
          .openConnection();
      conn.setInstanceFollowRedirects(false);
      conn.setDoOutput(true);
      conn.addRequestProperty("Content-Type",
          "application/x-www-form-urlencoded");

      conn.getResponseCode();
      String redirectURL = new String(conn.getHeaderField("Location"));
      conn = (HttpURLConnection) authUrl.resolve(redirectURL).toURL()
          .openConnection();
      conn.connect();

      String content = "";
      Source source = new Source(conn.getInputStream());
      for (Element element : source.getAllElements("form")) {
        if (element.getAttributeValue("action").equals(
            authorizer.getLoginFormValue())) {
          FormFields forms = element.getFormFields();
          forms.toArray();
          for (FormField field : forms) {
            switch (field.getFormControl().getFormControlType()) {
            case HIDDEN:
              if (field.getValues().size() > 0) {
                if (content.length() > 0)
                  content += "&";
                content += field.getName() + "=";
                for (String val : field.getValues())
                  content += URLEncoder.encode(val, "UTF-8");
              }
              break;
            default:
              break;
            }
          }

          content += authorizer.getLoginContentAddition();

          conn = (HttpURLConnection) authUrl.resolve(redirectURL).toURL()
              .openConnection();
          conn.setDoOutput(true);
          conn.setInstanceFollowRedirects(false);
          conn.addRequestProperty("Content-Type",
              "application/x-www-form-urlencoded");
          conn.getOutputStream().write(content.getBytes());
          conn.getResponseCode();
          URI token = new URI(conn.getHeaderField("Location"));

          conn = (HttpURLConnection) token.toURL().openConnection();
          String resultingToken = conn.getHeaderField("Location");
          source = new Source(conn.getInputStream());

          content = "";
          if (authorizer.isGrantAccessFormRequired()) {
            for (Element formElem : source.getAllElements("form")) {
              if (formElem.getAttributeValue("action").equals(
                  authorizer.getGrantAccessFormValue())) {
                for (FormField field : formElem.getFormFields()) {
                  switch (field.getFormControl().getFormControlType()) {
                  case HIDDEN:
                    if (field.getValues().size() > 0) {
                      if (content.length() > 0)
                        content += "&";
                      content += field.getName() + "=";
                      for (String val : field.getValues())
                        content += URLEncoder.encode(val, "UTF-8");
                    }
                    break;
                  default:
                    break;
                  }
                }
                content += authorizer.getGrantAccessContentAddition();
                conn = (HttpURLConnection) token
                    .resolve(authorizer.getGrantAccessFormValue()).toURL()
                    .openConnection();
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(false);
                conn.addRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
                conn.getOutputStream().write(content.getBytes());
                conn.getResponseCode();
                resultingToken = conn.getHeaderField("Location");
                return resultingToken.split("\\?")[1];
              }
            }
          } else {
            return resultingToken.split("\\?")[1];
          }
        }
      }
    } catch (URISyntaxException e) {
      e.printStackTrace();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
