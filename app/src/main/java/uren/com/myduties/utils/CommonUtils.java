package uren.com.myduties.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import uren.com.myduties.R;
import uren.com.myduties.models.TaskType;

import static uren.com.myduties.constants.StringConstants.APP_GOOGLE_PLAY_DEFAULT_LINK;

public class CommonUtils {


    public static void showToastShort(Context context, String message) {

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showToastLong(Context context, String message) {

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static String getDeviceID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static int getPaddingInPixels(Context context, float dpSize) {
        final float scale = context.getResources().getDisplayMetrics().density;
        int paddingInPx = (int) (dpSize * scale + 0.5f);
        return paddingInPx;
    }


    public static String getVersionName(Context context) {

        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return Objects.requireNonNull(pInfo).versionName;

    }

    public static String getVersion(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            String version = packInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "0";
        }
    }

    public static void commentApp(Context context) {
        try {
            String mAddress = "market://details?id=" + context.getPackageName();
            Intent marketIntent = new Intent("android.intent.action.VIEW");
            marketIntent.setData(Uri.parse(mAddress));
            context.startActivity(marketIntent);
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.commentFailed), Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean checkCameraHardware(Context context) {
        // this device has a camera
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static void hideKeyBoard(Context context) {
        Activity activity = (Activity) context;
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            Objects.requireNonNull(inputMethodManager).hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static String getGooglePlayAppLink(Context context) {
        return APP_GOOGLE_PLAY_DEFAULT_LINK + context.getPackageName();
    }

    public static String timeAgo(Context context, String createAt) {

        String convTime = "";
        Resources resources = context.getResources();
        //String suffix = resources.getString(R.string.ago);
        String suffix = "";

        Date nowTime = new Date();
        Date date = CommonUtils.fromISO8601UTC(createAt);

        long dateDiff = nowTime.getTime() - Objects.requireNonNull(date).getTime();

        long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
        long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
        long hour = TimeUnit.MILLISECONDS.toHours(dateDiff);
        long day = TimeUnit.MILLISECONDS.toDays(dateDiff);

        if (second < 60) {
            convTime = second + " " + resources.getString(R.string.seconds) + " " + suffix;
        } else if (minute < 60) {
            convTime = minute + " " + resources.getString(R.string.minutes) + " " + suffix;
        } else if (hour < 24) {
            convTime = hour + " " + resources.getString(R.string.hours) + " " + suffix;
        } else if (day >= 7) {
            if (day > 30) {
                convTime = (day / 30) + " " + resources.getString(R.string.months) + " " + suffix;
            } else if (day > 360) {
                convTime = (day / 360) + " " + resources.getString(R.string.years) + " " + suffix;
            } else {
                convTime = (day / 7) + " " + resources.getString(R.string.weeks) + " " + suffix;
            }
        } else if (day < 7) {
            convTime = day + " " + resources.getString(R.string.days) + " " + suffix;
        }

        return convTime;
    }

    public static String getMessageTime(Context context, long time) {
        String dateValueStr;
        String hour;

        Date date = new Date(time);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        format.setTimeZone(TimeZone.getDefault());
        String formatted = format.format(date);
        hour = formatted.substring(11, 16);

        Date todayDate = new Date(System.currentTimeMillis());
        String formattedTodayDate = format.format(todayDate);

        if (formatted.substring(0, 10).equals(formattedTodayDate.substring(0, 10)))
            dateValueStr = context.getResources().getString(R.string.TODAY);
        else if (isYesterday(date))
            dateValueStr = context.getResources().getString(R.string.YESTERDAY);
        else {
            String[] monthArray = context.getResources().getStringArray(R.array.months);
            String monthValue = monthArray[Integer.parseInt(formatted.substring(5, 7)) - 1];

            dateValueStr = formatted.substring(8, 10) + " "
                    + monthValue.substring(0, 3) +
                    " " + formatted.substring(0, 4);
        }

        return dateValueStr + "  " + hour;
    }


    public static boolean isYesterday(Date d) {
        return DateUtils.isToday(d.getTime() + DateUtils.DAY_IN_MILLIS);
    }

    public static Date fromISO8601UTC(String dateStr) {

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(tz);

        try {
            return df.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

    }

    public static void connectionErrSnackbarShow(View view, Context context) {
        Snackbar snackbar = Snackbar.make(view,
                context.getResources().getString(R.string.CHECK_YOUR_INTERNET_CONNECTION),
                Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(context.getResources().getColor(R.color.Red, null));
        TextView tv = snackBarView.findViewById(R.id.snackbar_text);
        tv.setTextColor(context.getResources().getColor(R.color.White, null));
        snackbar.show();
    }

    public static void snackbarShow(View view, Context context, String message, int colorId) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(context.getResources().getColor(colorId, null));
        TextView tv = snackBarView.findViewById(R.id.snackbar_text);
        tv.setTextColor(context.getResources().getColor(R.color.White, null));
        snackbar.show();
    }

    public static int getRandomColor(Context context) {

        int[] colorList = {
                R.color.yellow_green_color_picker,
                R.color.dot_light_screen2,
                R.color.PeachPuff,
                R.color.Gold,
                R.color.Pink,
                R.color.LightPink,
                R.color.dot_light_screen3,
                R.color.dot_light_screen4,
                R.color.dot_light_screen1,
                R.color.LemonChiffon,
                R.color.PapayaWhip,
                R.color.Wheat,
                R.color.Azure,
                R.color.PaleGoldenrod,
                R.color.Thistle,
                R.color.LightBlue,
                R.color.LightCoral,
                R.color.PaleGoldenrod,
                R.color.Violet,
                R.color.DarkSalmon,
                R.color.Lavender,
                R.color.Yellow,
                R.color.LightBlue,
                R.color.DarkGray,
                R.color.SharedPostEndColor,
                R.color.CaughtPostEndColor,
                R.color.Yellow,
                R.color.Violet,
                R.color.PaleGreen,
                R.color.LightCyan
        };

        Random rand = new Random();
        return colorList[rand.nextInt(colorList.length)];
    }

    public static int getDarkRandomColor(Context context) {

        int[] colorList = {
                R.color.style_color_primary,
                R.color.style_color_accent,
                R.color.fab_color_pressed,
                R.color.blue_color_picker,
                R.color.brown_color_picker,
                R.color.green_color_picker,
                R.color.orange_color_picker,
                R.color.red_color_picker,
                R.color.red_orange_color_picker,
                R.color.violet_color_picker,
                R.color.dot_dark_screen1,
                R.color.dot_dark_screen2,
                R.color.dot_dark_screen3,
                R.color.dot_dark_screen4,
                R.color.Fuchsia,
                R.color.DarkRed,
                R.color.Olive,
                R.color.Purple,
                R.color.gplus_color_1,
                R.color.gplus_color_2,
                R.color.gplus_color_3,
                R.color.gplus_color_4,
                R.color.MediumTurquoise,
                R.color.RoyalBlue,
                R.color.Green
        };

        Random rand = new Random();
        return colorList[rand.nextInt(colorList.length)];
    }

    public static String getLanguage() {
        String language = Locale.getDefault().getLanguage();
        return language;
    }

    public static Drawable setDrawableSelector(Context context, int normal, int selected) {

        StateListDrawable drawable;

        Drawable state_normal = ContextCompat.getDrawable(context, normal);
        Drawable state_pressed = ContextCompat.getDrawable(context, selected);

        Bitmap state_normal_bitmap = ((BitmapDrawable) Objects.requireNonNull(state_normal)).getBitmap();

        // Setting alpha directly just didn't work, so we draw a new bitmap!
        Bitmap disabledBitmap = Bitmap.createBitmap(
                state_normal.getIntrinsicWidth(),
                state_normal.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(disabledBitmap);

        Paint paint = new Paint();
        paint.setAlpha(126);
        canvas.drawBitmap(state_normal_bitmap, 0, 0, paint);

        BitmapDrawable state_normal_drawable = new BitmapDrawable(context.getResources(), disabledBitmap);

        drawable = new StateListDrawable();

        drawable.addState(new int[]{android.R.attr.state_selected},
                state_pressed);
        drawable.addState(new int[]{android.R.attr.state_enabled},
                state_normal_drawable);

        return drawable;
    }

    public static void setTaskTypeImage(Context context, ImageView taskTypeImgv, String type, TaskTypeHelper taskTypeHelper) {
        if (type == null || type.isEmpty()) return;
        int typeVal = 0;

        for (TaskType taskType : taskTypeHelper.getTypes())
            if (taskType.getKey().equals(type)) {
                typeVal = taskType.getImgId();
                break;
            }

        Glide.with(context)
                .load(typeVal)
                .apply(RequestOptions.centerInsideTransform())
                .into(taskTypeImgv);
    }

    public static void setUrgencyTv(boolean urgencyVal, TextView tvUrgency) {
        if (tvUrgency != null) {
            if (urgencyVal)
                tvUrgency.setVisibility(View.VISIBLE);
            else
                tvUrgency.setVisibility(View.GONE);
        }
    }

    public static void setClosedTv(boolean closed, TextView tvClosed) {
        if (tvClosed != null) {
            if (closed)
                tvClosed.setVisibility(View.VISIBLE);
            else
                tvClosed.setVisibility(View.GONE);
        }
    }

    public static void setCompletedImgv(Context context, boolean completed, ImageView completedImgv) {
        if (completed)
            completedImgv.setColorFilter(context.getResources().getColor(R.color.Green, null), PorterDuff.Mode.SRC_IN);
        else
            completedImgv.setColorFilter(context.getResources().getColor(R.color.Red, null), PorterDuff.Mode.SRC_IN);
    }

    public static String readCountryCodes(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.country_codes);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toString();
    }

    public static void showKeyboard(Context context, boolean showKeyboard, EditText editText) {

        if (showKeyboard) {
            InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(context).getSystemService(Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } else {
            InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(context).getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(editText.getWindowToken(), 0);
            editText.setFocusable(false);
            editText.setFocusableInTouchMode(true);
        }
    }
}