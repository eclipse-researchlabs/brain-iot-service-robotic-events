
package eu.brain.iot.robot.tables.jsonReader;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class CartTable {

    @SerializedName("Cart_Storages")
    @Expose
    private List<CartStorage> cartStorages = null;

    public List<CartStorage> getCartStorages() {
        return cartStorages;
    }

    public void setCartStorages(List<CartStorage> cartStorages) {
        this.cartStorages = cartStorages;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("cartStorages", cartStorages).toString();
    }

}
