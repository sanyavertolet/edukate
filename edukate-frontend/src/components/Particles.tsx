import { useEffect, useMemo, useState } from "react";
import Particles, { initParticlesEngine } from "@tsparticles/react";
import { type ISourceOptions, MoveDirection, OutMode } from "@tsparticles/engine";
import { loadSlim } from "@tsparticles/slim";
import { useTheme } from "./themes/ThemeContextProvider";
import themes from "./themes/themes";

export const ParticlesComponent = () => {
    const [init, setInit] = useState(false);

    const { theme } = useTheme();

    useEffect(() => {
        initParticlesEngine(async (engine) => {
            await loadSlim(engine);
        }).then(() => {
            setInit(true);
        });
    }, []);

    const options: ISourceOptions = useMemo(
        () => ({
            background: {
                color: { value: themes[theme].palette.background.default },
            },
            fullScreen: {
                zIndex: -1,
                enable: true
            },
            fpsLimit: 144,
            interactivity: {
                events: {
                    onHover: {
                        enable: true,
                        mode: "repulse",
                    },
                },
                modes: {
                    repulse: {
                        distance: 60,
                        duration: 0.2,
                    },
                },
            },
            particles: {
                color: {
                    value: "#851691",
                },
                links: {
                    color: "#007E8A",
                    distance: 125,
                    enable: true,
                    opacity: 0.25,
                    width: 1,
                },
                move: {
                    direction: MoveDirection.none,
                    enable: true,
                    outModes: {
                        default: OutMode.out,
                    },
                    random: false,
                    speed: 2,
                    straight: false,
                },
                number: {
                    density: {
                        enable: true,
                    },
                    value: 100,
                },
                opacity: {
                    value: 0.7,
                },
                shape: {
                    type: "circle",
                },
                size: {
                    value: { min: 3, max: 7 },
                },
                collisions: {
                    enable: true,
                    mode: "bounce",
                    absorb: {
                        speed: 0
                    }
                }
            },
            detectRetina: true,
        }),
        [theme],
    );

    if (init) {
        return (
            <Particles id="tsparticles" options={options}/>
        );
    }

    return <></>;
};
