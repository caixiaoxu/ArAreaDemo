package ng.dat.ar;

import android.os.Environment;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.model.LatLng;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Xuwl
 * @date 2021/9/3
 */
public class GeoRectifyingUtil {

    private int SPORT_MAX_SPEED = 5; // 最大运动时长
    private int isFirst = 0;         // 是否是第一次定位点
    private BPLocationLatLng weight1;        // 权重点1
    private BPLocationLatLng weight2;                          // 权重点2
    private List<BPLocationLatLng> w1TempList = new ArrayList<>();     // w1的临时定位点集合
    private List<BPLocationLatLng> w2TempList = new ArrayList<>();     // w2的临时定位点集合
    private int w1Count = 0; // 统计w1Count所统计过的点数

    /**
     * 判断偏移点算法
     *
     * @param aMapLocation 高德定位返回
     * @return 自定义稳定点的集合
     */
    public boolean filterPos(AMapLocation aMapLocation) {
        long currentStamp = System.currentTimeMillis() / 1000;
        String filterString = currentStamp + " :";
        try {
            // 获取的第一个定位点不进行过滤
            if (isFirst < 2) {
                isFirst++;
                if (isFirst < 2) {
                    filterString += "第一个定位点 : （容易漂移）不记录，不更新";
                    // 测试记录
                    weight1 = getLocation(aMapLocation, currentStamp);
                    return false;
                }
                float distance = AMapUtils.calculateLineDistance(
                        new LatLng(weight1.latitude, weight1.longitude),
                        new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()));
                weight1 = getLocation(aMapLocation, currentStamp);
                filterString += "第二个定位点 : 与第一个点的距离：" + distance + ", 重新开始记录，不更新 ,";
                // 将得到的第一个点存储入w1的缓存集合
                final BPLocationLatLng traceLocation = getLocation(aMapLocation, currentStamp);
                w1TempList.add(traceLocation);
                w1Count++;
                return true;
            } else {
                // 开始运动后，只针对GPS定位点且当前可用卫星数量>=8时才有效
                if (aMapLocation.getLocationType() <= AMapLocation.LOCATION_TYPE_SAME_REQ && aMapLocation.getSatellites() >= 8) {
                    filterString += "非第一次" + " : ";
                    // 过滤静止时的偏点，在静止时速度小于1米就算做静止状态
                    if (weight2 == null) {
                        filterString += "weight2=null" + " : ";
                        // 计算w1与当前定位点p1的时间差并得到最大偏移距离D
                        long offsetTime = currentStamp - weight1.timestamp;
                        if (offsetTime == 0) {
                            return false;
                        }
                        long MaxDistance = offsetTime * SPORT_MAX_SPEED;
                        float distance = AMapUtils.calculateLineDistance(
                                new LatLng(weight1.latitude, weight1.longitude),
                                new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()));
                        filterString += "distance=" + distance + ", MaxDistance=" + MaxDistance + " : ";
                        if (distance > MaxDistance) {
                            filterString += " distance>MaxDistance" + " 当前点 距离大: 设置w2位新的点，并添加到w2TempList";
                            // 将设置w2位新的点，并存储入w2临时缓存
                            weight2 = getLocation(aMapLocation, currentStamp);
                            w2TempList.add(weight2);
                            return false;
                        } else {
                            filterString += " distance<MaxDistance" + " 当前点 距离小 : 添加到w1TempList";
                            // 将p1加入到做坐标集合w1TempList
                            BPLocationLatLng traceLocation = getLocation(aMapLocation, currentStamp);
                            w1TempList.add(traceLocation);
                            w1Count++;
                            // 更新w1权值点
                            weight1.latitude = weight1.latitude * 0.2 + aMapLocation.getLatitude() * 0.8;
                            weight1.longitude = weight1.longitude * 0.2 + aMapLocation.getLongitude() * 0.8;
                            weight1.timestamp = currentStamp;
//						if (w1TempList.size() > 3) {
//							filterString += "d1TempList.size() > 3" + " : 更新";
//							// 将w1TempList中的数据放入finalList，并将w1TempList清空
//							mListPoint.addAll(w1TempList);
//							w1TempList.clear();
//							return true;
//						} else {
//							filterString += "d1TempList.size() < 3" + " : 不更新";
//							return false;
//						}
                            if (w1Count > 3) {
                                filterString += " : 更新";
                                List<BPLocationLatLng> list = new ArrayList<>(w1TempList);
                                w1TempList.clear();
                                return true;
                            } else {
                                filterString += " w1Count<3: 不更新";
                                return false;
                            }
                        }

                    } else {
                        filterString += "weight2 != null" + " : ";
                        // 计算w2与当前定位点p1的时间差并得到最大偏移距离D
                        long offsetTimes = currentStamp - weight2.timestamp;
                        long MaxDistance = offsetTimes * SPORT_MAX_SPEED;
                        float distance = AMapUtils.calculateLineDistance(
                                new LatLng(weight2.latitude, weight2.longitude),
                                new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()));
                        filterString += "distance = " + distance + ",MaxDistance = " + MaxDistance + " : ";
                        if (distance > MaxDistance) {
                            filterString += "当前点 距离大: weight2";
                            w2TempList.clear();
                            // 将设置w2位新的点，并存储入w2临时缓存
                            weight2 = getLocation(aMapLocation, currentStamp);
                            w2TempList.add(weight2);
                            return false;
                        } else {
                            filterString += "当前点 距离小: 添加到w2TempList";
                            // 将p1加入到做坐标集合w2TempList
                            BPLocationLatLng traceLocation = getLocation(aMapLocation, currentStamp);
                            w2TempList.add(traceLocation);

                            // 更新w2权值点
                            weight2.latitude = weight2.latitude * 0.2 + aMapLocation.getLatitude() * 0.8;
                            weight2.longitude = (weight2.longitude * 0.2 + aMapLocation.getLongitude() * 0.8);
                            weight2.timestamp = currentStamp;
                            if (w2TempList.size() > 4) {
                                List<BPLocationLatLng> list = new ArrayList<>();
                                // 判断w1所代表的定位点数是否>4,小于说明w1之前的点为从一开始就有偏移的点
                                if (w1Count > 4) {
                                    filterString += "w1Count > 4" + "计算增加W1";
                                    list.addAll(w1TempList);
                                } else {
                                    filterString += "w1Count < 4" + "计算丢弃W1";
                                    w1TempList.clear();
                                }
                                filterString += "w2TempList.size() > 4" + " : 更新";

                                // 将w2TempList集合中数据放入finalList中
                                list.addAll(w2TempList);
                                // 1、清空w2TempList集合 2、更新w1的权值点为w2的值 3、将w2置为null
                                w2TempList.clear();
                                weight1 = weight2;
                                weight2 = null;
                                return true;

                            } else {
                                filterString += "w2TempList.size() < 4";
                                return false;
                            }
                        }
                    }
                } else {
                    Log.d("定位", "定位成功" + ", 类型：" + aMapLocation.getLocationType() + "  ----- 丢弃当前定位点 ---");
                    return false;
                }
            }
        } finally {
            StringBuilder sb = new StringBuilder();
            if (aMapLocation.getErrorCode() == 0) {
                sb.append("定位成功" + ", 类型：" + aMapLocation.getLocationType() + "，");
//                sb.append("定位时间: ").append(AmapLLLLUtils.formatUTC(aMapLocation.getTime(), "yyyy-MM-dd HH:mm:ss")).append(", ");
            } else {
                //定位失败
                sb.append("定位失败" + ", ");
                sb.append("错误码:").append(aMapLocation.getErrorCode()).append(", ");
                sb.append("错误信息:").append(aMapLocation.getErrorInfo()).append(", ");
                sb.append("错误描述:").append(aMapLocation.getLocationDetail()).append(", ");
            }
            sb.append("---定位质量报告").append(": ");
            sb.append("WIFI开关：").append(aMapLocation.getLocationQualityReport().isWifiAble() ? "开启" : "关闭").append(", ");
//            sb.append("GPS状态：").append(getGPSStatusString(aMapLocation.getLocationQualityReport().getGPSStatus())).append(", ");
            sb.append("GPS星数：").append(aMapLocation.getLocationQualityReport().getGPSSatellites()).append(", ");
            sb.append("网络类型：").append(aMapLocation.getLocationQualityReport().getNetworkType()).append(", ");
            sb.append("网络耗时：").append(aMapLocation.getLocationQualityReport().getNetUseTime()).append(", ");
            filterString += ",\n------ " + sb.toString();
            try {
                write(filterString);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("定位信息", filterString);
        }
    }

    private void write(String content) throws IOException {
        //创建一个带缓冲区的输出流
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            File file = new File(Environment.getExternalStorageDirectory(), "data.txt");
            FileWriter writer = new FileWriter(file, true);
            writer.write(content);
            writer.close();
        }
    }

    private BPLocationLatLng getLocation(AMapLocation aMapLocation, long currentStamp) {
        BPLocationLatLng locationLatLng = new BPLocationLatLng();
        locationLatLng.latitude = aMapLocation.getLatitude();
        locationLatLng.longitude = aMapLocation.getLongitude();
        locationLatLng.altitude = (float) aMapLocation.getAltitude();
        locationLatLng.timestamp = currentStamp;
        locationLatLng.mapAccuracy = (int) (aMapLocation.getAccuracy() + 0.5f);
        locationLatLng.mapSatellites = aMapLocation.getSatellites();
        return locationLatLng;
    }

    public static class BPLocationLatLng implements Serializable {
        public double latitude;
        public double longitude;
        public long timestamp;
        public float altitude;
        public int mapAccuracy;
        public int mapSatellites;
    }
}
