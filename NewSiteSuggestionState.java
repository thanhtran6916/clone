package com.viettel.nims.optimalDesign.service.v2.suggestion.core.state;

import com.viettel.nims.optimalDesign.dto.SuggestStatusDTO;
import com.viettel.nims.optimalDesign.entity.Suggestion;
import com.viettel.nims.optimalDesign.entity.SuggestionCallOff;
import com.viettel.nims.optimalDesign.entity.SuggestionRadio;
import com.viettel.nims.optimalDesign.repository.SuggestionCallOffRepository;
import com.viettel.nims.optimalDesign.service.SuggestionCallOffService;
import com.viettel.nims.optimalDesign.service.v2.suggestion.core.helper.SuggestionRadioHelperService;
import com.viettel.nims.optimalDesign.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NewSiteSuggestionState {

  public static class Action {

    public static final String ACTION_APPROVE = "APPROVED";
    public static final String ACTION_REJECT = "REJECTED";
    public static final String ACTION_UPDATE_LOCATION = "UPDATE_LOCATION";
    public static final String ACTION_CREATE_LOCATION = "CREATE_LOCATION";
    public static final String ACTION_CREATE_CALLOFF = "CREATE_CALLOFF";
    public static final String ACTION_UPDATE_CALLOFF = "UPDATE_CALLOFF";
    public static final String ACTION_SIGN_VOFFICE = "SIGN_VOFFICE";
    public static final String ACTION_UPDATE_AND_CHANGE_LOCATION = "UPDATE_AND_CHANGE_LOCATION";
    public static final String ACTION_UPDATE_AND_NOT_CHANGE_LOCATION = "UPDATE_AND_NOT_CHANGE_LOCATION";
    public static final String ACTION_CANCEL = "UPDATE_CANCEL";
    public static final String ACTION_CREATE_RADIO = "CREATE_RADIO";
    public static final String ACTION_UPDATE_RADIO = "UPDATE_RADIO";
    public static final String ACTION_UPDATE_RADIO_CHANGE_SOLUTION = "UPDATE_RADIO_CHANGE_SOLUTION";
    public static final String ACTION_CREATE_REPEATER = "CREATE_REPEATER";
    public static final String ACTION_UPDATE_REPEATER = "UPDATE_REPEATER";
    public static final String ACTION_UPDATE_REPEATER_CHANGE_SOLUTION = "UPDATE_REPEATER_CHANGE_SOLUTION";
    public static final String ACTION_CREATE_TRANS = "CREATE_TRANS";
    public static final String ACTION_UPDATE_TRANS = "UPDATE_TRANS";
    public static final String ACTION_HANDOVER_INFRA = "HANDOVER_INFRA";
    public static final String ACTION_UPDATE_CALLOFF_CHANGE_SOLUTION = "UPDATE_CALLOFF_CHANGE_SOLUTION";


  }


  private final ApplicationContext context;
  private final SuggestionCallOffRepository suggestionCallOffRepository;
  private final SuggestionCallOffService suggestionCallOffService;
  private final SuggestionRadioHelperService suggestionRadioHelperService;

  public SgtState getNextState(Suggestion suggestion, String actionCode) {
    SgtState currentStatus = getStatus(suggestion.getSuggestStatus());
    Long suggestId = suggestion.getSuggestId();
    //
    SgtState nextStatus = currentStatus;

    switch (currentStatus.getSuggestionStatus()) {
      case Constants.SUGGEST_STATUS.DRAFT: {
        // neu chua co callOff Radio => giu nguyen trang thai
        SuggestionRadio suggestionRadio = suggestionRadioHelperService.getBySuggestionId(suggestId);
        if (suggestionRadio == null) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DRAFT);
          break;
        }
        List<SuggestionCallOff> suggestionCallOffs = suggestionCallOffRepository.findBySuggestIdAndTypeAndRowStatus(suggestId, Constants.SUGGESTION_CALL_OFF.TYPE.VALUE.TYPE_REPEATER,Constants.ROW_STATUS.ACTIVE);
        if(!CollectionUtils.isEmpty(suggestionCallOffs)){
          nextStatus = getStatus(Constants.SUGGEST_STATUS.TAO_MOI);
          break;
        }
        // neu co callOff => chuyen trang thai sang tao moi
        Integer stationTechType = suggestionRadio.getStationTechType();
        boolean isFullCallOff = suggestionCallOffService.isFullCallOff(suggestId, Constants.STATION_TECH_TYPE.getNames()[stationTechType]);
        if (isFullCallOff) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.TAO_MOI);
        }
        break;

      }

      case Constants.SUGGEST_STATUS.TAO_MOI: {
        if (Action.ACTION_APPROVE.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.BGD_PHE_DUYET_THIET_KE_DE_XUAT);
          break;
          //
        }
        if (Action.ACTION_REJECT.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.BGD_TU_CHOI_PHE_DUYET_THIET_KE_DE_XUAT);
          break;
        }
        SuggestionRadio suggestionRadioDTO = suggestionRadioHelperService.getBySuggestionId(suggestId);
        if (suggestionRadioDTO == null) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DRAFT);
        }
        //
        Integer stationTechType = suggestionRadioDTO.getStationTechType();
        boolean isFullCallOff = suggestionCallOffService.isFullCallOff(suggestId, Constants.STATION_TECH_TYPE.getNames()[stationTechType]);
        if (!isFullCallOff) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DRAFT);
        }
        break;
      }

      case Constants.SUGGEST_STATUS.BGD_TU_CHOI_PHE_DUYET_THIET_KE_DE_XUAT: {
        nextStatus = getStatus(Constants.SUGGEST_STATUS.TAO_MOI);
        break;
      }

      case Constants.SUGGEST_STATUS.BGD_PHE_DUYET_THIET_KE_DE_XUAT: {
        nextStatus = getStatus(Constants.SUGGEST_STATUS.CAP_NHAT_THIET_KE_SAU_KHAO_SAT);
        break;
      }

      case Constants.SUGGEST_STATUS.CAP_NHAT_THIET_KE_SAU_KHAO_SAT: {
        if (Action.ACTION_APPROVE.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.BGD_DONG_Y_THIET_KE_SAU_KHAO_SAT);
          break;
        }
        if (Action.ACTION_REJECT.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.BGD_TU_CHOI_THIET_KE_SAU_KHAO_SAT);
          break;
        }
        break;
      }
      case Constants.SUGGEST_STATUS.BGD_TU_CHOI_THIET_KE_SAU_KHAO_SAT: {
        nextStatus = getStatus(Constants.SUGGEST_STATUS.CAP_NHAT_THIET_KE_SAU_KHAO_SAT);
        break;
      }

      case Constants.SUGGEST_STATUS.BGD_DONG_Y_THIET_KE_SAU_KHAO_SAT: {
        if (Action.ACTION_APPROVE.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.HOAN_THANH_THAM_DINH);
          break;

        }
        if (Action.ACTION_REJECT.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.BGD_TU_CHOI_SAU_THAM_DINH);
          break;
        }
        if (Action.ACTION_UPDATE_CALLOFF.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.CAP_NHAT_THIET_KE_SAU_KHAO_SAT);
          break;
        }
        break;
      }
      case Constants.SUGGEST_STATUS.HOAN_THANH_THAM_DINH: {
        if (Action.ACTION_SIGN_VOFFICE.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DANG_TRINH_KY);
          break;
        }
        // Neu khong thay doi giap thi giu nguyen trang thai
        if (isChangeSolution(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.HOAN_THANH_THAM_DINH);
          break;
        }

        if (Action.ACTION_UPDATE_CALLOFF.equals(actionCode)) {
          // neu thay doi call-off => tham dinh lai
          nextStatus = getStatus(Constants.SUGGEST_STATUS.CAP_NHAT_THIET_KE_SAU_KHAO_SAT);
          break;
        }
        break;
      }
      case Constants.SUGGEST_STATUS.BGD_TU_CHOI_SAU_THAM_DINH: {
        // neu cap nhat call-off (khong co call off co trang thai reject) => chuyen ve
        if (isChangeCallOff(suggestId)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.CAP_NHAT_THIET_KE_SAU_KHAO_SAT);
          break;
        }
        break;
      }
      case Constants.SUGGEST_STATUS.DANG_TRINH_KY: {
        if (Action.ACTION_APPROVE.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DA_DUOC_PHE_DUYET_TREN_VOFFICE);
          break;

        }
        if (Action.ACTION_REJECT.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DA_BI_TU_CHOI_TREN_VOFFICE);
          break;
        }
        break;
      }
      case Constants.SUGGEST_STATUS.DA_DUOC_PHE_DUYET_TREN_VOFFICE: {
        nextStatus = getStatus(Constants.SUGGEST_STATUS.DA_BAN_GIAO_HA_TANG);
        break;
      }
      case Constants.SUGGEST_STATUS.DA_BI_TU_CHOI_TREN_VOFFICE: {
        nextStatus = getStatus(Constants.SUGGEST_STATUS.CAP_NHAT_THIET_KE_SAU_KHAO_SAT);
        break;
      }

      case Constants.SUGGEST_STATUS.DA_BAN_GIAO_HA_TANG: {
        // kiem tra vi tri
        if (Action.ACTION_UPDATE_AND_CHANGE_LOCATION.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DE_XUAT_THAY_DOI_GIAI_PHAP);
          break;
        } else {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DA_CAP_NHAT_CALL_OFF_THUC_TE);
          break;
        }
      }
      case Constants.SUGGEST_STATUS.DA_CAP_NHAT_CALL_OFF_THUC_TE: {
        // kiem tra vi tri
        if (Action.ACTION_UPDATE_AND_CHANGE_LOCATION.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DE_XUAT_THAY_DOI_GIAI_PHAP);
        } else if (Action.ACTION_APPROVE.equals(actionCode)) {
          // đây neh
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DA_PHE_DUYET_CALL_OFF_THUC_TE);
        } else if (Action.ACTION_REJECT.equals(actionCode)) {
          // chính nó
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DA_TU_CHOI_CALL_OFF_THUC_TE);
        } else {
          nextStatus = currentStatus;
        }
        break;
      }
      case Constants.SUGGEST_STATUS.DE_XUAT_THAY_DOI_GIAI_PHAP: {
        // kiem tra vi tri
        if (Action.ACTION_UPDATE_AND_NOT_CHANGE_LOCATION.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DA_CAP_NHAT_CALL_OFF_THUC_TE);
        } else if (Action.ACTION_SIGN_VOFFICE.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DANG_TRINH_KY_CALL_OFF_SAU_THAY_DOI_SAU_GIAI_PHAP);
        } else {
          nextStatus = currentStatus;
        }
        break;
      }
      case Constants.SUGGEST_STATUS.DANG_TRINH_KY_CALL_OFF_SAU_THAY_DOI_SAU_GIAI_PHAP: {
        if (Action.ACTION_APPROVE.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DA_PHE_DUYET_CALL_OFF_SAU_THAY_DOI_SAU_GIAI_PHAP);
        } else if (Action.ACTION_REJECT.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DA_BI_TU_CHOI_CALL_OFF_SAU_THAY_DOI_SAU_GIAI_PHAP);
        } else {
          nextStatus = currentStatus;
        }
        break;
      }
      case Constants.SUGGEST_STATUS.DA_PHE_DUYET_CALL_OFF_SAU_THAY_DOI_SAU_GIAI_PHAP: {
        nextStatus = getStatus(Constants.SUGGEST_STATUS.DA_CAP_NHAT_THIET_KE_THI_CONG);
        break;
      }
      case Constants.SUGGEST_STATUS.DA_BI_TU_CHOI_CALL_OFF_SAU_THAY_DOI_SAU_GIAI_PHAP: {
        nextStatus = getStatus(Constants.SUGGEST_STATUS.DA_CAP_NHAT_CALL_OFF_THUC_TE);
        break;
      }
      case Constants.SUGGEST_STATUS.DA_TU_CHOI_CALL_OFF_THUC_TE: {
        if (Action.ACTION_UPDATE_AND_CHANGE_LOCATION.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DE_XUAT_THAY_DOI_GIAI_PHAP);
        } else {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DA_CAP_NHAT_CALL_OFF_THUC_TE);
        }
        break;
      }
      case Constants.SUGGEST_STATUS.DA_PHE_DUYET_CALL_OFF_THUC_TE:
      case Constants.SUGGEST_STATUS.DA_TU_CHOI_THIET_KE_THI_CONG: {
        nextStatus = getStatus(Constants.SUGGEST_STATUS.DA_CAP_NHAT_THIET_KE_THI_CONG);
        break;
      }
      case Constants.SUGGEST_STATUS.DA_CAP_NHAT_THIET_KE_THI_CONG: {
        if (Action.ACTION_APPROVE.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DA_PHE_DUYET_THIET_KE_THI_CONG);
        } else if (Action.ACTION_REJECT.equals(actionCode)) {
          nextStatus = getStatus(Constants.SUGGEST_STATUS.DA_TU_CHOI_THIET_KE_THI_CONG);
        } else {
          nextStatus = currentStatus;
        }
        break;
      }
      default:
        nextStatus = currentStatus;
    }

    return nextStatus;

  }


  private SgtState getStatus(int suggestionStatus) {
    return context.getBean("NewSite_" + suggestionStatus, SgtState.class);
  }

  private boolean isChangeSolution(String action) {
    return Action.ACTION_UPDATE_CALLOFF_CHANGE_SOLUTION.equals(action)
        || Action.ACTION_UPDATE_REPEATER_CHANGE_SOLUTION.equals(action)
        || Action.ACTION_UPDATE_RADIO_CHANGE_SOLUTION.equals(action);
  }


  private boolean isChangeCallOff(Long suggestId) {
    boolean checkChange = true;
    List<SuggestionCallOff> suggestionCallOffs = suggestionCallOffRepository.findBySuggestIdAndCallOffType(suggestId, Constants.SUGGESTION_CALL_OFF.CALL_OFF_TYPE.CALL_OFF_SURVEY);

    for (SuggestionCallOff callOff : suggestionCallOffs) {
      if (callOff.getCallOffStatus() == Constants.SUGGESTION_CALL_OFF.CALL_OFF_STATUS.REJECT) {
        checkChange = false;
        break;
      }
    }
    return checkChange;
  }

}
