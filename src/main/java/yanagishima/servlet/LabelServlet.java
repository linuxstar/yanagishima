package yanagishima.servlet;

import static yanagishima.repository.TinyOrm.value;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.repository.TinyOrm;
import yanagishima.model.db.Label;

@Slf4j
@Singleton
public class LabelServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final TinyOrm db;
    private final YanagishimaConfig config;

    @Inject
    public LabelServlet(TinyOrm db, YanagishimaConfig config) {
        this.db = db;
        this.config = config;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }

            String engine = getRequiredParameter(request, "engine");
            String queryId = getRequiredParameter(request, "queryid");
            String labelName = getRequiredParameter(request, "labelName");

            int count = db.insert(Label.class, value("datasource", datasource),
                                  value("engine", engine),
                                  value("query_id", queryId),
                                  value("label_name", labelName));

            responseBody.put("datasource", datasource);
            responseBody.put("engine", engine);
            responseBody.put("queryid", queryId);
            responseBody.put("labelName", labelName);
            responseBody.put("count", count);

        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> responseBody = new HashMap<>();

        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }

            String engine = getRequiredParameter(request, "engine");
            String queryId = getRequiredParameter(request, "queryid");
            Optional<Label> optionalLabel = db.singleLabel("datasource = ? and engine = ? and query_id = ?", datasource, engine, queryId);
            if (optionalLabel.isPresent()) {
                responseBody.put("label", optionalLabel.get().getLabelName());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }

            String engine = getRequiredParameter(request, "engine");
            String queryId = getRequiredParameter(request, "queryid");
            db.singleLabel("datasource = ? and engine = ? and query_id = ?", datasource, engine, queryId).ifPresent(Label::delete);
            responseBody.put("datasource", datasource);
            responseBody.put("engine", engine);
            responseBody.put("queryid", queryId);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }
}
