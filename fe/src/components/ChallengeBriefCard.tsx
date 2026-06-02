import { BookOutlined, CaretRightOutlined } from "@ant-design/icons";
import { Collapse, Space, Tag, Typography } from "antd";
import type { ChallengeDetail } from "../api/types";
import CtlCard from "./ui/CtlCard";
import { difficultyColor } from "../utils/difficulty";
import { formatRuntimeLabel } from "../utils/languageRuntimes";

type Props = {
  challenge: ChallengeDetail;
  runtimeVersion: string;
};

export default function ChallengeBriefCard({ challenge, runtimeVersion }: Props) {
  return (
    <CtlCard title={challenge.title}>
      <Space wrap className="mb-3">
        <Tag color={difficultyColor(challenge.difficulty)}>{challenge.difficulty}</Tag>
        <Tag color="blue">{formatRuntimeLabel(challenge.language, runtimeVersion)}</Tag>
        <Typography.Text className="!text-slate-400">
          {challenge.publicTests.length} public · {challenge.hiddenTestCount} hidden
        </Typography.Text>
      </Space>

      <Collapse
        bordered={false}
        className="challenge-brief-collapse bg-transparent"
        defaultActiveKey={["instructions"]}
        expandIcon={({ isActive }) => (
          <CaretRightOutlined rotate={isActive ? 90 : 0} className="!text-slate-400" />
        )}
        items={[
          {
            key: "instructions",
            label: (
              <Space>
                <BookOutlined className="text-emerald-400" />
                <span className="text-slate-200">Instructions</span>
              </Space>
            ),
            children: (
              <pre className="max-h-56 overflow-auto whitespace-pre-wrap text-sm text-slate-300">
                {challenge.descriptionMd}
              </pre>
            ),
          },
          ...(challenge.publicTests.length > 0
            ? [
                {
                  key: "public-tests",
                  label: (
                    <span className="text-slate-200">
                      Public tests ({challenge.publicTests.length})
                    </span>
                  ),
                  children: (
                    <ul className="m-0 list-none space-y-3 p-0 text-sm text-slate-300">
                      {challenge.publicTests.map((test) => (
                        <li key={test.name}>
                          <span className="font-medium text-slate-200">{test.name}</span>
                          {test.description ? (
                            <p className="mb-0 mt-1 text-slate-400">{test.description}</p>
                          ) : null}
                        </li>
                      ))}
                    </ul>
                  ),
                },
              ]
            : []),
        ]}
      />
    </CtlCard>
  );
}
