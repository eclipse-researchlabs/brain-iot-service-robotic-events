
package eu.brain.iot.robot.tables.jsonReader;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class CartStorage {

    @SerializedName("cartID")
    @Expose
    private String cartID;
    @SerializedName("storageID")
    @Expose
    private String storageID;

    public String getCartID() {
        return cartID;
    }

    public void setCartID(String cartID) {
        this.cartID = cartID;
    }

    public String getStorageID() {
        return storageID;
    }

    public void setStorageID(String storageID) {
        this.storageID = storageID;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("cartID", cartID).append("storageID", storageID).toString();
    }

}
