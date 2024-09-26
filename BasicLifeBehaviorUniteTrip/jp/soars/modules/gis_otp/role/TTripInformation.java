package jp.soars.modules.gis_otp.role;

import jp.soars.core.TSpot;
import jp.soars.modules.gis_otp.otp.TOtpResult.EOtpStatus;
import jp.soars.modules.gis_otp.otp.TOtpState;

/**
 * 移動情報クラス．
 * 移動開始日，移動開始時，移動開始分，移動終了日，移動終了時，移動終了分，出発地スポット，目的地スポット，
 * 経路ID，移動手段，徒歩時間，徒歩距離を保持している．
 */
public class TTripInformation {

    /** 検索状態 */
    private EOtpStatus fSearchStatus;

    /** ログ用 */
    public TOtpState fOtpState;

    /** データ */
    private TData fData;

    public class TData {
        /** 移動開始日 */
        private int fStartDay;

        /** 移動開始時 */
        private int fStartHour;

        /** 移動開始分 */
        private int fStartMinute;

        /** 移動終了日 */
        private int fEndDay;

        /** 移動終了時 */
        private int fEndHour;

        /** 移動終了分 */
        private int fEndMinute;

        /** 出発地スポット */
        private TSpot fOrigin;

        /** 目的地スポット */
        private TSpot fDestination;

        /** 経路ID */
        private int fRouteID;

        /** 移動手段 */
        private String fModes;

        /** 徒歩時間 */
        private int fTimeForWalking;

        /** 徒歩距離 */
        private double fDistanceForWalking;

        /**
         * コンストラクタ
         * @param startDay 移動開始日
         * @param startHour 移動開始時
         * @param startMinute 移動開始分
         * @param endDay 移動終了日
         * @param endHour 移動終了時
         * @param endMinute 移動終了分
         * @param origin 出発地スポット
         * @param destination 目的地スポット
         * @param routeID 経路ID
         * @param modes 移動手段
         * @param timeForWalking 徒歩時間
         * @param distanceForWalking 徒歩距離
        */
        public TData(int startDay, int startHour, int startMinute, 
                     int endDay, int endHour, int endMinute, 
                     TSpot origin, TSpot destination, int routeID,
                     String modes, int timeForWalking, double distanceForWalking) {
            fStartDay = startDay;
            fStartHour = startHour;
            fStartMinute = startMinute;
            fEndDay = endDay;
            fEndHour = endHour;
            fEndMinute = endMinute;
            fOrigin = origin;
            fDestination = destination; 
            fRouteID = routeID;
            fModes = modes;
            fTimeForWalking = timeForWalking;
            fDistanceForWalking = distanceForWalking;
        }

        @Override
        public String toString() {
            return fStartDay + ":" + fStartHour + ":" + fStartMinute + "," + fEndDay + ":" + fEndHour + ":" + fEndMinute + "," + fOrigin + "," + fDestination + "," + fRouteID + "," + fModes + "," + fTimeForWalking + "," + fDistanceForWalking;
        }

        /**
         * 移動開始日を返す．
         * @return 移動開始日
         */
        public int getStartDay() {
            return fStartDay;
        }

        /**
         * 移動開始時を返す．
         * @return 移動開始時
         */
        public int getStartHour() {
            return fStartHour;
        }

        /**
         * 移動開始分を返す．
         * @return 移動開始分
         */
        public int getStartMinute() {
            return fStartMinute;
        }

        /**
         * 移動終了日を返す．
         * @return 移動終了日
         */
        public int getEndDay() {
            return fEndDay;
        }

        /**
         * 移動終了時を返す．
         * @return 移動終了時
         */
        public int getEndHour() {
            return fEndHour;
        }

        /**
         * 移動終了分を返す．
         * @return 移動終了分
         */
        public int getEndMinute() {
            return fEndMinute;
        }

        /**
         * 出発地スポットを返す．
         * @return 出発地スポット
         */
        public TSpot getOrigin() {
            return fOrigin;
        }

        /**
         * 目的地スポットを返す．
         * @return 目的地スポット
         */
        public TSpot getDestination() {
            return fDestination;
        }

        /**
         * 経路IDを返す．
         * @return 経路ID
         */
        public int getRouteID() {
            return fRouteID;
        }

        /**
         * 移動手段を返す．
         * @return 移動手段
         */
        public String getModes() {
            return fModes;
        }

        /**
         * 徒歩時間を返す．
         * @return 徒歩時間
         */
        public int getTimeForWalking() {
            return fTimeForWalking;
        }

        /**
         * 徒歩距離を返す．
         * @return 徒歩距離
         */
        public double getDistanceForWalking() {
            return fDistanceForWalking;
        }
    }


    /**
     * コンストラクタ
     * @param searchStatus 検索結果
     * @param startDay 移動開始日
     * @param startHour 移動開始時
     * @param startMinute 移動開始分
     * @param endDay 移動終了日
     * @param endHour 移動終了時
     * @param endMinute 移動終了分
     * @param origin 出発地スポット
     * @param destination 目的地スポット
     * @param routeID 経路ID
     * @param modes 移動手段
     * @param timeForWalking 徒歩時間
     * @param distanceForWalking 徒歩距離
     */
    public TTripInformation(EOtpStatus searchStatus, int startDay, int startHour, int startMinute, 
                             int endDay, int endHour, int endMinute, 
                             TSpot origin, TSpot destination, int routeID,
                             String modes, int timeForWalking, double distanceForWalking) {
        fSearchStatus = searchStatus;
        fData = new TData(startDay, startHour, startMinute, endDay, endHour, endMinute, 
                          origin, destination, routeID, modes, timeForWalking, distanceForWalking);            
    }

    /**
     * コンストラクタ
     * @param searchStatus 検索結果
     */
    public TTripInformation(EOtpStatus searchStatus) {
        fSearchStatus = searchStatus;
        fData = null;
    }

    @Override
    public String toString() {
        if (fSearchStatus == EOtpStatus.SUCCESS) {
            return fSearchStatus + "," + fData;            
        } else {
            return fSearchStatus + "";
        }
    }

    /**
     * 検索状態を返す．
     * @return 検索状態
     */
    public EOtpStatus getSearchStatus() {
        return fSearchStatus;
    }

    /**
     * 移動開始日を返す．
     * @return 移動開始日
     */
    public int getStartDay() {
        if (fSearchStatus == EOtpStatus.SUCCESS) {
            return fData.getStartDay();
        } else {
            throw new RuntimeException("Search status is no EOtpStatus.SUCCESS");
        }
    }

    /**
     * 移動開始時を返す．
     * @return 移動開始時
     */
    public int getStartHour() {
        if (fSearchStatus == EOtpStatus.SUCCESS) {
            return fData.getStartHour();
        } else {
            throw new RuntimeException("Search status is not EOtpStatus.SUCCESS");
        }
    }

    /**
     * 移動開始分を返す．
     * @return 移動開始分
     */
    public int getStartMinute() {
        if (fSearchStatus == EOtpStatus.SUCCESS) {
            return fData.getStartMinute();
        } else {
            throw new RuntimeException("Search status is no EOtpStatus.SUCCESS");
        }
    }

    /**
     * 移動終了日を返す．
     * @return 移動終了日
     */
    public int getEndDay() {
        if (fSearchStatus == EOtpStatus.SUCCESS) {
            return fData.getEndDay();
        } else {
            throw new RuntimeException("Search status is no EOtpStatus.SUCCESS");
        }
    }

    /**
     * 移動終了時を返す．
     * @return 移動終了時
     */
    public int getEndHour() {
        if (fSearchStatus == EOtpStatus.SUCCESS) {
            return fData.getEndHour();
        } else {
            throw new RuntimeException("Search status is no EOtpStatus.SUCCESS");
        }
    }

    /**
     * 移動終了分を返す．
     * @return 移動終了分
     */
    public int getEndMinute() {
        if (fSearchStatus == EOtpStatus.SUCCESS) {
            return fData.getEndMinute();
        } else {
            throw new RuntimeException("Search status is no EOtpStatus.SUCCESS");
        }
    }

    /**
     * 出発地スポットを返す．
     * @return 出発地スポット
     */
    public TSpot getOrigin() {
        if (fSearchStatus == EOtpStatus.SUCCESS) {
            return fData.getOrigin();
        } else {
            throw new RuntimeException("Search status is no EOtpStatus.SUCCESS");
        }
    }

    /**
     * 目的地スポットを返す．
     * @return 目的地スポット
     */
    public TSpot getDestination() {
        if (fSearchStatus == EOtpStatus.SUCCESS) {
            return fData.getDestination();
        } else {
            throw new RuntimeException("Search status is no EOtpStatus.SUCCESS");
        }
    }

    /**
     * 経路IDを返す．
     * @return 経路ID
     */
    public int getRouteID() {
        if (fSearchStatus == EOtpStatus.SUCCESS) {
            return fData.getRouteID();
        } else {
            throw new RuntimeException("Search status is no EOtpStatus.SUCCESS");
        }
    }

    /**
     * 移動手段を返す．
     * @return 移動手段
     */
    public String getModes() {
        if (fSearchStatus == EOtpStatus.SUCCESS) {
            return fData.getModes();
        } else {
            throw new RuntimeException("Search status is no EOtpStatus.SUCCESS");
        }
    }

    /**
     * 徒歩時間を返す．
     * @return 徒歩時間
     */
    public int getTimeForWalking() {
        if (fSearchStatus == EOtpStatus.SUCCESS) {
            return fData.getTimeForWalking();
        } else {
            throw new RuntimeException("Search status is no EOtpStatus.SUCCESS");
        }
    }

    /**
     * 徒歩距離を返す．
     * @return 徒歩距離
     */
    public double getDistanceForWalking() {
        if (fSearchStatus == EOtpStatus.SUCCESS) {
            return fData.getDistanceForWalking();
        } else {
            throw new RuntimeException("Search status is no EOtpStatus.SUCCESS");
        }
    }
}
