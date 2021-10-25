package org.jlab.dtm.business.service;

import org.jlab.dtm.persistence.util.DtmSqlUtil;
import org.jlab.smoothness.business.util.IOUtil;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MigrateOldRarService {

    public List<OldRARRecord> getOldRecords(int year) throws SQLException {
        List<OldRARRecord> recordList = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "select * from rer_owner.reports where request_date >= to_date('" + year + "-01', 'yyyy-mm') and request_date < to_date('" + (year + 1) + "-01', 'yyyy-mm') order by request_date asc";

        System.err.println("sql: " + sql);

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql);

            rs = stmt.executeQuery();

            while (rs.next()) {
                Date requestDate = rs.getDate("request_date");
                String title = rs.getString("title");
                String groupName = rs.getString("leader_group");
                int staffId = rs.getInt("leader");
                int rarId = rs.getInt("report_id");
                OldRARRecord record = new OldRARRecord();

                record.requestDate = requestDate;
                record.title = title;
                record.groupName = groupName;
                record.staffId = staffId;
                record.rarId = rarId;

                recordList.add(record);
            }
        } finally {
            IOUtil.close(rs, stmt, con);
        }

        return recordList;
    }

    public AttachmentRecord getAttachment(int rarId) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "select file_name, mime_type, data from rer_owner.attachments where report_id = " + rarId;
        AttachmentRecord record = null;

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql);

            rs = stmt.executeQuery();

            if (rs.next()) {
                String filename = rs.getString("file_name");
                String contentType = rs.getString("mime_type");
                InputStream in = rs.getBinaryStream("data");

                record = new AttachmentRecord();
                record.filename = filename;
                record.contentType = contentType;
                record.in = in;
            }
        } finally {
            IOUtil.close(rs, stmt, con);
        }

        return record;
    }

    public class AttachmentRecord {
        public String filename;
        public String contentType;
        public InputStream in;
    }

    public class OldRARRecord {
        public String title;
        public java.util.Date requestDate;
        public int rarId;
        public int staffId;
        public String groupName;
    }
}
