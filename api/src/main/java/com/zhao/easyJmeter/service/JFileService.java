package com.zhao.easyJmeter.service;

import com.zhao.easyJmeter.model.JFileDO;
import com.zhao.easyJmeter.vo.CutFileVO;
import com.zhao.easyJmeter.vo.JFileVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface JFileService {

    JFileVO createFile(MultipartFile file);

    Boolean setFileCut(Integer id, Boolean cut);

    String downloadFile(Integer id, String dir);

    void downloadCutFile(List<CutFileVO> cutFileVOList, String dir);

    List<JFileDO> createCsvCutFiles(Map<Integer, List<String>> fileMp);

    JFileDO searchById(Integer id);

    Boolean needCut(String[] fileIds);

    JFileDO createFile(String filePath);

    Boolean updateById(JFileDO jFileDO);

    void downLoadJmeterLogZip(String taskId, OutputStream outputStream) throws IOException;

    List<JFileDO> searchJtlByTaskId(String taskId);

    String getStoreDir();
}
