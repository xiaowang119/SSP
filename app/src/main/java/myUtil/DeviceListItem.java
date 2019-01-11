package myUtil;

public class DeviceListItem {
    private int drawable_id;
    private int name_id;

    public DeviceListItem(int name_id, int drawable_id) {
        this.name_id = name_id;
        this.drawable_id = drawable_id;
    }

    public void setName_id(int name_id) {
        this.name_id = name_id;
    }

    public void setDrawable_id(int drawable_id) {
        this.drawable_id = drawable_id;
    }

    public int getName_id() {
        return this.name_id;
    }

    public int getDrawable_id() {
        return drawable_id;
    }

}
