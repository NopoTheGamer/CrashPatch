package org.polyfrost.crashpatch.mixin;

//#if FORGE
import org.polyfrost.crashpatch.crashes.StateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;

//TODO: this could be completely useless, check with smarter people
@Mixin(TileEntityRendererDispatcher.class)
public abstract class MixinTileEntityRendererDispatcher implements StateManager.IResettable {

    @Unique
    private boolean crashpatch$drawingBatch = false;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onInit(CallbackInfo ci) {
        StateManager.INSTANCE.getResettableRefs().add(new WeakReference<>(this));
    }

    @Override
    public void resetState() {
        if (crashpatch$drawingBatch) crashpatch$drawingBatch = false;
    }

    @Redirect(method = "renderTileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;hasFastRenderer()Z"))
    private boolean isNotFastRenderOrDrawing(TileEntity instance) {
        if (!crashpatch$drawingBatch) {
            return false;
        } else {
            return instance.hasFastRenderer();
        }
    }

    @Redirect(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntity;DDDFI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;hasFastRenderer()Z"))
    private boolean isFastRenderOrDrawing(TileEntity instance) {
        return crashpatch$drawingBatch && instance.hasFastRenderer();
    }

    @Inject(method = "preDrawBatch", at = @At("TAIL"), remap = false)
    private void setDrawingBatchTrue(CallbackInfo ci) {
        crashpatch$drawingBatch = true;
    }

    @Inject(method = "drawBatch", at = @At("TAIL"), remap = false)
    private void setDrawingBatchFalse(int pass, CallbackInfo ci) {
        crashpatch$drawingBatch = false;
    }

}
//#endif
