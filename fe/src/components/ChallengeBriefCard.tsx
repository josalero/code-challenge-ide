import { BookOutlined, CaretRightOutlined } from "@ant-design/icons";
import { Collapse, Space, Tag, Typography } from "antd";
import type { ChallengeDetail } from "../api/types";
import CtlCard from "./ui/CtlCard";
import { difficultyColor } from "../utils/difficulty";

type Props = {
  challenge: ChallengeDetail;
  runtimeVersion: string;
};

export default function ChallengeBriefCard({ challenge, runtimeVersion }: Props) {
  return (
    <CtlCard title={challenge.title}>
      <Space wrap className="mb-3">
        <Tag color={difficultyColor(challenge.difficulty)}>{challenge.difficulty}</Tag>
        <Tag color="blue">Java {runtimeVersion}</Tag>
        <Typography.Text className="!text-slate-400">
          {challenge.publicTestNames.length} public · {challenge.hiddenTestCount} hidden
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
          ...(challenge.publicTestNames.length > 0
            ? [
                {
                  key: "public-tests",
                  label: (
                    <span className="text-slate-200">
                      Public tests ({challenge.publicTestNames.length})
                    </span>
                  ),
                  children: (
                    <ul className="list-inside list-disc text-sm text-slate-300">
                      {challenge.publicTestNames.map((name) => (
                        <li key={name}>{name}</li>
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
