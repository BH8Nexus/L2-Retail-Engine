// package l2r.gameserver.cache;
//
// import l2r.gameserver.Config;
// import l2r.gameserver.idfactory.IdFactory;
// import l2r.gameserver.utils.DDSConverter;
//
// import java.io.File;
// import java.nio.ByteBuffer;
// import java.util.Arrays;
// import java.util.HashMap;
// import java.util.LinkedList;
// import java.util.List;
// import java.util.Map;
// import java.util.concurrent.locks.Lock;
// import java.util.concurrent.locks.ReentrantReadWriteLock;
// import java.util.regex.Pattern;
//
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
//
// import javolution.util.FastMap;
//
//
// public class ImagesChache
// {
// private static final Logger _log = LoggerFactory.getLogger(ImagesChache.class);
//
// private final static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
// private final static Lock readLock = lock.readLock();
//
// public static final Pattern HTML_PATTERN = Pattern.compile("%image:(.*?)%", 32);
//
// private static final ImagesChache _instance = new ImagesChache();
//
// private final Map<String, Integer> _imagesId = new HashMap<>();
//
// private final FastMap<Integer, byte[]> _images = new FastMap<>();
//
// public static final ImagesChache getInstance()
// {
// return _instance;
// }
//
// private ImagesChache()
// {
// load();
// }
//
// public void reload()
// {
// try
// {
// if ((_imagesId != null) && (_images != null))
// {
// _imagesId.clear();
// _images.clear();
// load();
// }
// }
// catch (Exception e)
// {
// _log.error("ImagesChache: Error while Reloading image cache.", e);
// }
//
// }
//
// public void load()
// {
// try
// {
// _log.info("ImagesChache: Loading images...");
//
// File folder = new File(Config.DATAPACK_ROOT, "data/images");
//
// if (!folder.exists() || !folder.isDirectory())
// {
// _log.info("ImagesChache: Files missing, loading aborted.");
// return;
// }
//
// // Count files inside folders too.
// List<File> files = new LinkedList<>();
// files.addAll(Arrays.asList(folder.listFiles()));
// for (File file : folder.listFiles())
// {
// if (file.isDirectory())
// {
// files.addAll(Arrays.asList(file.listFiles()));
// }
// }
//
// int count = 0;
// int folders = 1;
// for (File file : files)
// {
// if (file.isDirectory())
// {
// folders++;
// continue;
// }
//
// if (checkImageFormat(file))
// {
// count++;
//
// String fileName = file.getName();
// ByteBuffer bf = DDSConverter.convertToDDS(file);
// byte[] image = bf.array();
// int imageId = IdFactory.getInstance().getNextId();
//
// if (_imagesId.containsKey(fileName.toLowerCase()))
// {
// _log.warn("Duplicate image name \"" + fileName + "\". Replacing with " + file.getPath());
// }
//
// _imagesId.put(fileName.toLowerCase(), Integer.valueOf(imageId));
// _images.put(imageId, image);
// }
// }
//
// _log.info("ImagesChache: Loaded " + count + " images from " + (folders == 1 ? (folders + " folder.") : (folders + " folders.")));
// }
// catch (Exception e)
// {
// _log.warn("Error while loading custom images:", e);
// }
// }
//
// public int getImageId(String val)
// {
// int imageId = 0;
//
// readLock.lock();
// try
// {
// if (_imagesId.get(val.toLowerCase()) != null)
// {
// imageId = _imagesId.get(val.toLowerCase()).intValue();
// }
// }
// finally
// {
// readLock.unlock();
// }
//
// return imageId;
// }
//
// public byte[] getImage(int imageId)
// {
// byte[] image = null;
//
// readLock.lock();
// try
// {
// image = _images.get(imageId);
// }
// finally
// {
// readLock.unlock();
// }
//
// return image;
// }
//
// public FastMap<Integer, byte[]> getChachedImages()
// {
// return _images;
// }
//
// private static boolean checkImageFormat(File file)
// {
// String filename = file.getName();
// int dotPos = filename.lastIndexOf(".");
// String format = filename.substring(dotPos);
// if (format.equalsIgnoreCase(".jpg") || format.equalsIgnoreCase(".png") || format.equalsIgnoreCase(".bmp"))
// {
// return true;
// }
// return false;
// }
// }
