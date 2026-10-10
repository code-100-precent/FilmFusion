package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.ConfirmFileDTO;
import cn.cxdproject.coder.model.dto.PresignRequestDTO;
import cn.cxdproject.coder.model.vo.FileVO;
import cn.cxdproject.coder.model.vo.PresignVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
   FileVO imageUpload(MultipartFile file);

   /**
    * 生成前端直传 COS 的预签名凭证
    */
   PresignVO presign(PresignRequestDTO dto);

   /**
    * 前端直传完成后确认，校验对象已落 COS 并返回访问地址
    */
   FileVO confirm(ConfirmFileDTO dto);
}
