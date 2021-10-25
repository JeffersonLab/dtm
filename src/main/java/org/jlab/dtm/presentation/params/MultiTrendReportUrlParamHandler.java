package org.jlab.dtm.presentation.params;

import org.jlab.dtm.business.params.MultiTrendReportParams;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ParamBuilder;
import org.jlab.smoothness.presentation.util.ServletUtil;
import org.jlab.smoothness.presentation.util.UrlParamHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MultiTrendReportUrlParamHandler implements
        UrlParamHandler<MultiTrendReportParams> {

    private final HttpServletRequest request;
    private final Date today;
    private final Date sevenDaysAgo;

    public MultiTrendReportUrlParamHandler(HttpServletRequest request, Date today,
                                           Date sevenDaysAgo) {
        this.request = request;
        this.today = today;
        this.sevenDaysAgo = sevenDaysAgo;
    }

    @Override
    public MultiTrendReportParams convert() {
        String[] labelArray = null;
        Date[] startArray = null;
        Date[] endArray = null;

        try {
            labelArray = request.getParameterValues("label");

            if(labelArray != null && labelArray.length > 0) {

                String[] startStrArray = request.getParameterValues("start");
                String[] endStrArray = request.getParameterValues("end");

                if (startStrArray == null || startStrArray.length != labelArray.length || endStrArray == null || endStrArray.length != labelArray.length) {
                    throw new RuntimeException("label, start, and end parameters must have same cardinality");
                }

                startArray = new Date[labelArray.length];
                endArray = new Date[labelArray.length];

                for (int i = 0; i < labelArray.length; i++) {
                    Date start = DtmParamConverter.convertJLabDateTime(startStrArray[i]);
                    Date end = DtmParamConverter.convertJLabDateTime(endStrArray[i]);
                    startArray[i] = start;
                    endArray[i] = end;
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse date", e);
        }

        String type = request.getParameter("type");
        String size = request.getParameter("size");

        MultiTrendReportParams params = new MultiTrendReportParams();

        params.setLabelArray(labelArray);
        params.setStartArray(startArray);
        params.setEndArray(endArray);

        params.setType(type);
        params.setSize(size);

        return params;
    }

    @Override
    public void validate(MultiTrendReportParams params) {
    }

    @Override
    public void store(MultiTrendReportParams params) {
        /* Note: We store each field indivdually as we want to re-use amoung screens*/
 /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
 /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
        HttpSession session = request.getSession(true);

        session.setAttribute("startMulti[]", params.getStartArray());
        session.setAttribute("endMulti[]", params.getEndArray());
        session.setAttribute("label[]", params.getLabelArray());
        session.setAttribute("size[]", new String[]{params.getSize()});
    }

    @Override
    public MultiTrendReportParams defaults() {
        MultiTrendReportParams defaultParams = new MultiTrendReportParams();

        defaultParams.setStartArray(new Date[]{sevenDaysAgo});
        defaultParams.setEndArray(new Date[]{today});
        defaultParams.setLabelArray(new String[]{"Past 7 Days"});

        defaultParams.setType("line");
        defaultParams.setSize("day");

        return defaultParams;
    }

    @Override
    public MultiTrendReportParams materialize() {
        MultiTrendReportParams defaultValues = defaults();

        /* Note: We store each field indivdually as we want to re-use amoung screens*/
 /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
 /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
        HttpSession session = request.getSession(true);
        Date[] startArray = (Date[]) session.getAttribute("startMulti[]");
        Date[] endArray = (Date[]) session.getAttribute("endMulti[]");
        String[] labelArray = (String[]) session.getAttribute("label[]");
        String[] sizeArray = (String[]) session.getAttribute("size[]");

        String type = defaultValues.getType();
        String size = defaultValues.getSize();

        if (startArray == null) {
            startArray = defaultValues.getStartArray();
        }

        if (endArray == null) {
            endArray = defaultValues.getEndArray();
        }

        if(labelArray == null) {
            labelArray = defaultValues.getLabelArray();
        }

        if(sizeArray != null && sizeArray.length > 0) {
            size = sizeArray[0];
        }

        MultiTrendReportParams params = new MultiTrendReportParams();

        params.setStartArray(startArray);
        params.setEndArray(endArray);
        params.setLabelArray(labelArray);
        params.setType(type);
        params.setSize(size);

        return params;
    }

    @Override
    public boolean qualified() {
        return request.getParameter("qualified") != null;
    }

    @Override
    public String message(MultiTrendReportParams params) {
        String message = "";

            if (params.getLabelArray() != null && params.getLabelArray().length > 0) {
                message = message + params.getLabelArray()[0] + " {" + TimeUtil.formatSmartRangeSeparateTime(params.getStartArray()[0], params.getEndArray()[0]) + "}";
                for (int i = 1; i < params.getLabelArray().length; i++) {
                    message = message + ", " + params.getLabelArray()[i] + " {" + TimeUtil.formatSmartRangeSeparateTime(params.getStartArray()[i], params.getEndArray()[i]) + "}";
                }
            }

        return message;
    }

    /**
     * Sends a redirect response indicating the qualified URL. If calling this method from a Servlet
     * doGet method generally a return statement should immediately follow. This method is useful to
     * maintain a restful / bookmarkable URL for the user.
     *
     * @param response The Servlet response
     * @param params The parameter object
     * @throws IOException If unable to redirect
     */
    @Override
    public void redirect(HttpServletResponse response, MultiTrendReportParams params) throws
            IOException {
        ParamBuilder builder = new ParamBuilder();

        SimpleDateFormat dateFormat
                = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

        builder.add("start", nullOrFormatMultiple(params.getStartArray(), dateFormat));
        builder.add("end", nullOrFormatMultiple(params.getEndArray(), dateFormat));
        builder.add("label", params.getLabelArray());
        builder.add("type", IOUtil.nullOrString(params.getType()));
        builder.add("size", IOUtil.nullOrString(params.getSize()));
        builder.add("qualified", "");

        String url = ServletUtil.getCurrentUrlAdvanced(request, builder.getParams());

        response.sendRedirect(
                response.encodeRedirectURL(url));
    }

    public static Object[] nullOrFormatMultiple(Date[] input, SimpleDateFormat dateFormat) {
        Object[] resultArray = new Object[input.length];

        for(int i = 0; i < input.length; i++) {
            resultArray[i] = IOUtil.nullOrFormat(input[i], dateFormat);
        }

        return resultArray;
    }
}
