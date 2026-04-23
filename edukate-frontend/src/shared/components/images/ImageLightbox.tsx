import { FC, ReactNode } from "react";
import Lightbox, { SlideImage } from "yet-another-react-lightbox";
import Zoom from "yet-another-react-lightbox/plugins/zoom";
import "yet-another-react-lightbox/styles.css";

/**
 * Annotation layer renderer — reserved for the future drawing/annotation feature.
 * When implemented, this will render as an absolutely-positioned overlay inside
 * the slide via render.slideFooter, preserving YARL's zoom/pan gesture handling.
 * The overlay defaults to pointerEvents:none; annotation canvas opts in to
 * pointerEvents:auto only when drawing mode is active.
 */
export type AnnotationLayerRenderer = (props: { slide: SlideImage; rect: { width: number; height: number } }) => ReactNode;

interface ImageLightboxProps {
    /** All image URLs that can be navigated between with swipe / arrow keys. */
    images: string[];
    /** Zero-based index of the image to open. Pass -1 (or set open=false) to close. */
    index: number;
    /** Controls whether the lightbox is shown. */
    open: boolean;
    /** Called when the lightbox requests closing (close button, Escape, backdrop click). */
    onClose: () => void;
    /**
     * Reserved for the annotation feature — not yet wired.
     * TODO: wire to render.slideFooter when annotation ships.
     */
    renderAnnotationLayer?: AnnotationLayerRenderer;
}

export const ImageLightbox: FC<ImageLightboxProps> = ({
    images,
    index,
    open,
    onClose,
    renderAnnotationLayer: _renderAnnotationLayer, // eslint-disable-line @typescript-eslint/no-unused-vars
}) => {
    const slides: SlideImage[] = images.map((src) => ({ src }));

    return (
        <Lightbox
            open={open}
            close={onClose}
            index={index}
            slides={slides}
            plugins={[Zoom]}
            zoom={{
                maxZoomPixelRatio: 4,
                zoomInMultiplier: 2,
                doubleTapDelay: 300,
                doubleClickDelay: 300,
                scrollToZoom: true,
            }}
        />
    );
};
